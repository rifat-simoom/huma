from datetime import datetime, timedelta
from airflow import DAG
from airflow.operators.python import PythonOperator
from airflow.operators.email import EmailOperator
from airflow.operators.dummy import DummyOperator
from airflow.operators.bash import BashOperator
from airflow.models import Variable
from airflow.hooks.postgres_hook import PostgresHook
import requests
import json
import logging

# Default arguments for the DAG
default_args = {
    'owner': 'hrm-system',
    'depends_on_past': False,
    'start_date': datetime(2024, 1, 1),
    'email_on_failure': True,
    'email_on_retry': False,
    'retries': 1,
    'retry_delay': timedelta(minutes=5),
    'catchup': False
}

# DAG Definition
dag = DAG(
    'leave_approval_workflow',
    default_args=default_args,
    description='Leave Request Approval Workflow',
    schedule_interval=None,  # Manual trigger only
    max_active_runs=1,
    template_searchpath=['/opt/airflow/templates'],
    params={
        'leave_request_id': '',
        'employee_id': '',
        'manager_id': '',
        'leave_type': '',
        'days_requested': 0,
        'notification_settings': {
            'send_email': True,
            'send_slack': False,
            'send_teams': False
        },
        'approval_rules': {
            'auto_approve_threshold': 2,
            'require_hr_approval': False,
            'require_manager_approval': True
        }
    }
)

def get_leave_request_details(**context):
    """Fetch leave request details from the database"""
    leave_request_id = context['params']['leave_request_id']
    
    postgres_hook = PostgresHook(postgres_conn_id='hrm_postgres')
    
    query = """
    SELECT lr.*, e.first_name, e.last_name, e.email, e.manager_id,
           m.first_name as manager_first_name, m.last_name as manager_last_name, 
           m.email as manager_email, d.name as department_name
    FROM leave_requests lr
    JOIN employees e ON lr.employee_id = e.id
    LEFT JOIN employees m ON e.manager_id = m.id
    LEFT JOIN departments d ON e.department_id = d.id
    WHERE lr.id = %s
    """
    
    result = postgres_hook.get_first(query, parameters=[leave_request_id])
    
    if not result:
        raise ValueError(f"Leave request {leave_request_id} not found")
    
    # Convert to dictionary for easier access
    columns = [desc[0] for desc in postgres_hook.get_conn().cursor().description]
    leave_data = dict(zip(columns, result))
    
    # Store in XCom for other tasks
    context['task_instance'].xcom_push(key='leave_request_data', value=leave_data)
    
    logging.info(f"Retrieved leave request: {leave_data}")
    return leave_data

def validate_leave_request(**context):
    """Validate leave request against business rules"""
    leave_data = context['task_instance'].xcom_pull(key='leave_request_data')
    approval_rules = context['params']['approval_rules']
    
    validation_errors = []
    
    # Check if employee has sufficient leave balance
    if leave_data['leave_type'] == 'ANNUAL':
        if leave_data['days_requested'] > leave_data['annual_leave_balance']:
            validation_errors.append("Insufficient annual leave balance")
    elif leave_data['leave_type'] == 'SICK':
        if leave_data['days_requested'] > leave_data['sick_leave_balance']:
            validation_errors.append("Insufficient sick leave balance")
    
    # Check if manager exists for approval
    if approval_rules['require_manager_approval'] and not leave_data['manager_id']:
        validation_errors.append("No manager assigned for approval")
    
    # Check for overlapping leave requests
    postgres_hook = PostgresHook(postgres_conn_id='hrm_postgres')
    overlap_query = """
    SELECT COUNT(*) FROM leave_requests 
    WHERE employee_id = %s 
    AND status IN ('APPROVED', 'IN_PROGRESS')
    AND (
        (start_date <= %s AND end_date >= %s) OR
        (start_date <= %s AND end_date >= %s) OR
        (start_date >= %s AND end_date <= %s)
    )
    AND id != %s
    """
    
    overlap_count = postgres_hook.get_first(overlap_query, parameters=[
        leave_data['employee_id'],
        leave_data['start_date'], leave_data['start_date'],
        leave_data['end_date'], leave_data['end_date'],
        leave_data['start_date'], leave_data['end_date'],
        leave_data['id']
    ])[0]
    
    if overlap_count > 0:
        validation_errors.append("Overlapping leave requests found")
    
    if validation_errors:
        # Update leave request status to rejected
        update_query = """
        UPDATE leave_requests 
        SET status = 'REJECTED', 
            approver_comments = %s,
            updated_at = CURRENT_TIMESTAMP
        WHERE id = %s
        """
        postgres_hook.run(update_query, parameters=[
            f"Validation failed: {'; '.join(validation_errors)}",
            leave_data['id']
        ])
        raise ValueError(f"Validation failed: {'; '.join(validation_errors)}")
    
    logging.info("Leave request validation passed")
    return True

def auto_approve_check(**context):
    """Check if leave request can be auto-approved"""
    leave_data = context['task_instance'].xcom_pull(key='leave_request_data')
    approval_rules = context['params']['approval_rules']
    
    auto_approve_threshold = approval_rules['auto_approve_threshold']
    
    # Auto-approve if days requested is within threshold
    if leave_data['days_requested'] <= auto_approve_threshold:
        # Update leave request status to approved
        postgres_hook = PostgresHook(postgres_conn_id='hrm_postgres')
        update_query = """
        UPDATE leave_requests 
        SET status = 'APPROVED', 
            approver_comments = 'Auto-approved based on company policy',
            approved_date = CURRENT_DATE,
            updated_at = CURRENT_TIMESTAMP
        WHERE id = %s
        """
        postgres_hook.run(update_query, parameters=[leave_data['id']])
        
        logging.info(f"Leave request {leave_data['id']} auto-approved")
        return 'send_approval_notification'
    else:
        logging.info(f"Leave request {leave_data['id']} requires manual approval")
        return 'send_manager_notification'

def send_manager_notification(**context):
    """Send notification to manager for approval"""
    leave_data = context['task_instance'].xcom_pull(key='leave_request_data')
    notification_settings = context['params']['notification_settings']
    
    if not leave_data['manager_email']:
        raise ValueError("Manager email not found")
    
    # Update leave request status to pending manager approval
    postgres_hook = PostgresHook(postgres_conn_id='hrm_postgres')
    update_query = """
    UPDATE leave_requests 
    SET status = 'PENDING', 
        approver_id = %s,
        updated_at = CURRENT_TIMESTAMP
    WHERE id = %s
    """
    postgres_hook.run(update_query, parameters=[
        leave_data['manager_id'],
        leave_data['id']
    ])
    
    # Send notification (email, Slack, Teams based on settings)
    if notification_settings['send_email']:
        send_email_notification(leave_data, 'manager_approval')
    
    if notification_settings['send_slack']:
        send_slack_notification(leave_data, 'manager_approval')
    
    if notification_settings['send_teams']:
        send_teams_notification(leave_data, 'manager_approval')
    
    logging.info(f"Manager notification sent for leave request {leave_data['id']}")
    return True

def send_approval_notification(**context):
    """Send approval notification to employee"""
    leave_data = context['task_instance'].xcom_pull(key='leave_request_data')
    notification_settings = context['params']['notification_settings']
    
    # Send notification to employee
    if notification_settings['send_email']:
        send_email_notification(leave_data, 'approved')
    
    if notification_settings['send_slack']:
        send_slack_notification(leave_data, 'approved')
    
    # Update employee leave balance
    postgres_hook = PostgresHook(postgres_conn_id='hrm_postgres')
    
    if leave_data['leave_type'] == 'ANNUAL':
        balance_query = """
        UPDATE employees 
        SET annual_leave_balance = annual_leave_balance - %s,
            updated_at = CURRENT_TIMESTAMP
        WHERE id = %s
        """
    elif leave_data['leave_type'] == 'SICK':
        balance_query = """
        UPDATE employees 
        SET sick_leave_balance = sick_leave_balance - %s,
            updated_at = CURRENT_TIMESTAMP
        WHERE id = %s
        """
    else:
        balance_query = None
    
    if balance_query:
        postgres_hook.run(balance_query, parameters=[
            leave_data['days_requested'],
            leave_data['employee_id']
        ])
    
    logging.info(f"Approval notification sent for leave request {leave_data['id']}")
    return True

def send_rejection_notification(**context):
    """Send rejection notification to employee"""
    leave_data = context['task_instance'].xcom_pull(key='leave_request_data')
    notification_settings = context['params']['notification_settings']
    
    # Send notification to employee
    if notification_settings['send_email']:
        send_email_notification(leave_data, 'rejected')
    
    if notification_settings['send_slack']:
        send_slack_notification(leave_data, 'rejected')
    
    logging.info(f"Rejection notification sent for leave request {leave_data['id']}")
    return True

def send_email_notification(leave_data, notification_type):
    """Send email notification"""
    # Implementation depends on email service configuration
    # This is a placeholder for the actual email sending logic
    logging.info(f"Email notification sent: {notification_type} for leave request {leave_data['id']}")

def send_slack_notification(leave_data, notification_type):
    """Send Slack notification"""
    # Implementation depends on Slack webhook configuration
    logging.info(f"Slack notification sent: {notification_type} for leave request {leave_data['id']}")

def send_teams_notification(leave_data, notification_type):
    """Send Microsoft Teams notification"""
    # Implementation depends on Teams webhook configuration
    logging.info(f"Teams notification sent: {notification_type} for leave request {leave_data['id']}")

def cleanup_workflow(**context):
    """Cleanup workflow data"""
    leave_request_id = context['params']['leave_request_id']
    
    # Update workflow completion status
    postgres_hook = PostgresHook(postgres_conn_id='hrm_postgres')
    update_query = """
    UPDATE leave_requests 
    SET airflow_dag_run_id = %s,
        updated_at = CURRENT_TIMESTAMP
    WHERE id = %s
    """
    postgres_hook.run(update_query, parameters=[
        context['dag_run'].run_id,
        leave_request_id
    ])
    
    logging.info(f"Workflow cleanup completed for leave request {leave_request_id}")
    return True

# Task definitions
start_task = DummyOperator(
    task_id='start_workflow',
    dag=dag
)

fetch_details_task = PythonOperator(
    task_id='fetch_leave_details',
    python_callable=get_leave_request_details,
    dag=dag
)

validate_task = PythonOperator(
    task_id='validate_leave_request',
    python_callable=validate_leave_request,
    dag=dag
)

auto_approve_task = PythonOperator(
    task_id='auto_approve_check',
    python_callable=auto_approve_check,
    dag=dag
)

manager_notification_task = PythonOperator(
    task_id='send_manager_notification',
    python_callable=send_manager_notification,
    dag=dag
)

approval_notification_task = PythonOperator(
    task_id='send_approval_notification',
    python_callable=send_approval_notification,
    dag=dag
)

rejection_notification_task = PythonOperator(
    task_id='send_rejection_notification',
    python_callable=send_rejection_notification,
    dag=dag
)

cleanup_task = PythonOperator(
    task_id='cleanup_workflow',
    python_callable=cleanup_workflow,
    dag=dag
)

end_task = DummyOperator(
    task_id='end_workflow',
    dag=dag
)

# Task dependencies
start_task >> fetch_details_task >> validate_task >> auto_approve_task
auto_approve_task >> manager_notification_task >> cleanup_task >> end_task
auto_approve_task >> approval_notification_task >> cleanup_task >> end_task
validate_task >> rejection_notification_task >> cleanup_task >> end_task