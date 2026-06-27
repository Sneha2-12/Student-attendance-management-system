// Common utility functions and AJAX integrations

// Handle Logout
function logout() {
    fetch('/api/auth/logout', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        }
    })
    .then(response => {
        if (response.ok) {
            window.location.href = '/login';
        } else {
            console.error('Logout failed');
        }
    })
    .catch(error => {
        console.error('Error logging out:', error);
    });
}

// Handle Student Leave Application Submit
function submitLeaveApplication(event) {
    event.preventDefault();
    
    const leaveType = document.getElementById('leaveType').value;
    const startDate = document.getElementById('startDate').value;
    const endDate = document.getElementById('endDate').value;
    const reason = document.getElementById('reason').value;
    const documentPath = document.getElementById('documentPath').value; // In a full app, this might be a file upload path
    
    const alertEl = document.getElementById('leaveAlert');
    alertEl.className = 'alert';
    alertEl.style.display = 'none';

    if (!startDate || !endDate || !reason) {
        alertEl.innerText = "Please fill in all required fields.";
        alertEl.classList.add('alert-danger');
        alertEl.style.display = 'block';
        return;
    }

    const payload = { leaveType, startDate, endDate, reason, documentPath };

    fetch('/api/leaves/apply', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload)
    })
    .then(async response => {
        const data = await response.json();
        if (response.ok) {
            alertEl.innerText = "Leave application submitted successfully!";
            alertEl.classList.add('alert-success');
            alertEl.style.display = 'block';
            
            // Clear form
            document.getElementById('leaveForm').reset();
            
            // Reload page after 1.5s to show updated table
            setTimeout(() => {
                window.location.reload();
            }, 1500);
        } else {
            alertEl.innerText = data.message || "Failed to submit leave request.";
            alertEl.classList.add('alert-danger');
            alertEl.style.display = 'block';
        }
    })
    .catch(error => {
        console.error('Error submitting leave:', error);
        alertEl.innerText = "An error occurred. Please try again.";
        alertEl.classList.add('alert-danger');
        alertEl.style.display = 'block';
    });
}

// Handle Processing Leave Requests (Approve/Reject)
function processLeaveRequest(leaveId, status) {
    if (!confirm(`Are you sure you want to mark this leave request as ${status}?`)) {
        return;
    }

    fetch(`/api/leaves/process/${leaveId}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ status: status })
    })
    .then(async response => {
        if (response.ok) {
            alert(`Leave request successfully ${status.toLowerCase()}!`);
            window.location.reload();
        } else {
            const err = await response.text();
            alert('Failed to process leave request: ' + err);
        }
    })
    .catch(error => {
        console.error('Error processing leave:', error);
        alert('An error occurred. Please try again.');
    });
}

// Handle Marking Attendance Submit (Teacher Dashboard)
function submitAttendance(event) {
    event.preventDefault();

    const subjectId = document.getElementById('subjectSelect').value;
    const date = document.getElementById('attendanceDate').value;
    
    if (!subjectId || !date) {
        alert("Please select a subject and date.");
        return;
    }

    const studentStatuses = {};
    const rows = document.querySelectorAll('#attendanceTableBody tr');
    
    if (rows.length === 0) {
        alert("No students found in this section.");
        return;
    }

    rows.forEach(row => {
        const studentId = row.dataset.studentId;
        const statusRadio = row.querySelector(`input[name="status_${studentId}"]:checked`);
        if (statusRadio) {
            studentStatuses[studentId] = statusRadio.value;
        }
    });

    const payload = {
        subjectId: parseInt(subjectId),
        date: date,
        studentStatuses: studentStatuses
    };

    fetch('/api/attendance/mark', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload)
    })
    .then(async response => {
        if (response.ok) {
            alert("Attendance marked successfully!");
            window.location.reload();
        } else {
            const err = await response.text();
            alert("Failed to mark attendance: " + err);
        }
    })
    .catch(error => {
        console.error("Error marking attendance:", error);
        alert("An error occurred. Please try again.");
    });
}

// Setup Teacher Attendance Grid Filter
function setupAttendanceGrid() {
    const subjectSelect = document.getElementById('subjectSelect');
    const dateInput = document.getElementById('attendanceDate');
    const classFilter = document.getElementById('classFilter');

    if (!subjectSelect || !dateInput || !classFilter) return;

    const filterGrid = () => {
        const selectedClass = classFilter.value;
        const rows = document.querySelectorAll('#attendanceTableBody tr');
        
        rows.forEach(row => {
            const studentClass = row.dataset.classSection;
            if (!selectedClass || studentClass === selectedClass) {
                row.style.display = '';
            } else {
                row.style.display = 'none';
            }
        });
    };

    classFilter.addEventListener('change', filterGrid);
}

// Initialize on load
document.addEventListener('DOMContentLoaded', () => {
    // Bind forms if they exist
    const leaveForm = document.getElementById('leaveForm');
    if (leaveForm) {
        leaveForm.addEventListener('submit', submitLeaveApplication);
    }

    const attendanceForm = document.getElementById('attendanceForm');
    if (attendanceForm) {
        attendanceForm.addEventListener('submit', submitAttendance);
        setupAttendanceGrid();
    }
});
