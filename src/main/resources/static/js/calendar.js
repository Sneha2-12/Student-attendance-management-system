// Interactive Calendar Javascript for Upcoming Exams

let currentMonth = new Date().getMonth();
let currentYear = new Date().getFullYear();
let examsList = [];

// Fetch exams on startup
function fetchAndRenderExams() {
    fetch('/api/exams')
        .then(response => response.json())
        .then(data => {
            examsList = data;
            renderCalendar(currentMonth, currentYear);
        })
        .catch(error => {
            console.error('Error fetching exams:', error);
            renderCalendar(currentMonth, currentYear); // Render calendar anyway even if empty
        });
}

// Render the calendar grid
function renderCalendar(month, year) {
    const calendarMonthYear = document.getElementById('calendarMonthYear');
    const calendarDays = document.getElementById('calendarDays');
    
    if (!calendarDays) return;

    calendarDays.innerHTML = '';
    
    const monthNames = [
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    ];
    
    calendarMonthYear.innerText = `${monthNames[month]} ${year}`;
    
    // Day of week of the first day of the month
    const firstDay = new Date(year, month, 1).getDay();
    // Number of days in the month
    const daysInMonth = new Date(year, month + 1, 0).getDate();
    
    // Add empty slots for days before the first day of the month
    // Sunday is 0, Monday is 1, etc. Adjust for grid starting on Sunday
    for (let i = 0; i < firstDay; i++) {
        const emptyCell = document.createElement('div');
        emptyCell.classList.add('calendar-day-empty');
        calendarDays.appendChild(emptyCell);
    }
    
    // Render the days of the month
    for (let day = 1; day <= daysInMonth; day++) {
        const dayCell = document.createElement('div');
        dayCell.classList.add('calendar-day-cell');
        
        const dateStr = `${year}-${String(month + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
        
        const dateNum = document.createElement('span');
        dateNum.classList.add('day-number');
        dateNum.innerText = day;
        dayCell.appendChild(dateNum);

        // Highlight today
        const today = new Date();
        if (day === today.getDate() && month === today.getMonth() && year === today.getFullYear()) {
            dayCell.classList.add('today');
        }

        // Find exams on this date
        const dayExams = examsList.filter(exam => exam.examDate === dateStr);
        
        if (dayExams.length > 0) {
            dayCell.classList.add('has-exam');
            
            const examContainer = document.createElement('div');
            examContainer.classList.add('exam-badges-container');
            
            dayExams.forEach(exam => {
                const badge = document.createElement('div');
                badge.classList.add('calendar-exam-badge');
                badge.innerText = exam.subject.courseCode;
                badge.title = `${exam.subject.name} (${exam.startTime} - ${exam.endTime})`;
                
                badge.addEventListener('click', (e) => {
                    e.stopPropagation(); // Stop cell click
                    showExamDetails(exam);
                });
                
                examContainer.appendChild(badge);
            });
            
            dayCell.appendChild(examContainer);
        }
        
        // Let users click cells to add exam (if they are teacher/admin)
        dayCell.addEventListener('click', () => {
            const role = document.body.dataset.userRole;
            if (role === 'ROLE_ADMIN' || role === 'ROLE_TEACHER') {
                openAddExamModal(dateStr);
            }
        });
        
        calendarDays.appendChild(dayCell);
    }
}

// Show Exam Details in a sidebar or modal
function showExamDetails(exam) {
    const detailsContainer = document.getElementById('examDetailsContent');
    const panel = document.getElementById('examDetailsPanel');
    
    if (!detailsContainer || !panel) return;

    detailsContainer.innerHTML = `
        <div class="exam-detail-item">
            <h3>Subject</h3>
            <p>${exam.subject.name} (${exam.subject.courseCode})</p>
        </div>
        <div class="exam-detail-item">
            <h3>Date & Time</h3>
            <p>${exam.examDate}</p>
            <p>${exam.startTime} - ${exam.endTime}</p>
        </div>
        <div class="exam-detail-item">
            <h3>Venue / Room</h3>
            <p>${exam.room}</p>
        </div>
        <div class="exam-detail-item">
            <h3>Total Marks</h3>
            <p>${exam.totalMarks} Marks</p>
        </div>
        <div class="exam-detail-item">
            <h3>Invigilator</h3>
            <p>${exam.subject.teacher.name}</p>
        </div>
    `;

    // Add delete button for admins/teachers
    const role = document.body.dataset.userRole;
    if (role === 'ROLE_ADMIN' || role === 'ROLE_TEACHER') {
        const btnDelete = document.createElement('button');
        btnDelete.className = 'btn btn-danger';
        btnDelete.style.marginTop = '20px';
        btnDelete.innerText = 'Cancel Exam';
        btnDelete.onclick = () => cancelExam(exam.id);
        detailsContainer.appendChild(btnDelete);
    }

    panel.classList.add('active');
}

// Close Exam Details Panel
function closeExamDetails() {
    const panel = document.getElementById('examDetailsPanel');
    if (panel) panel.classList.remove('active');
}

// Cancel Exam (Delete API)
function cancelExam(examId) {
    if (!confirm('Are you sure you want to cancel and delete this exam?')) return;

    fetch(`/api/exams/${examId}`, {
        method: 'DELETE'
    })
    .then(response => {
        if (response.ok) {
            alert('Exam cancelled successfully.');
            closeExamDetails();
            fetchAndRenderExams(); // Refresh calendar
        } else {
            alert('Failed to cancel exam.');
        }
    })
    .catch(error => {
        console.error('Error deleting exam:', error);
        alert('An error occurred. Please try again.');
    });
}

// Open Add Exam Modal
function openAddExamModal(dateStr) {
    const modal = document.getElementById('addExamModal');
    const dateInput = document.getElementById('examDateInput');
    
    if (!modal || !dateInput) return;
    
    dateInput.value = dateStr;
    modal.classList.add('active');
}

// Close Add Exam Modal
function closeAddExamModal() {
    const modal = document.getElementById('addExamModal');
    if (modal) modal.classList.remove('active');
}

// Submit Add Exam form (REST POST)
function submitAddExam(event) {
    event.preventDefault();
    
    const subjectId = document.getElementById('examSubjectSelect').value;
    const examDate = document.getElementById('examDateInput').value;
    const startTime = document.getElementById('examStartTime').value;
    const endTime = document.getElementById('examEndTime').value;
    const room = document.getElementById('examRoom').value;
    const totalMarks = document.getElementById('examMarks').value;

    if (!subjectId || !examDate || !startTime || !endTime || !room || !totalMarks) {
        alert('Please fill in all fields.');
        return;
    }

    const payload = {
        subjectId: parseInt(subjectId),
        examDate,
        startTime: startTime + ":00", // Append seconds for LocalTime mapping
        endTime: endTime + ":00",
        room,
        totalMarks: parseInt(totalMarks)
    };

    fetch('/api/exams/schedule', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload)
    })
    .then(async response => {
        if (response.ok) {
            alert('Exam scheduled successfully!');
            closeAddExamModal();
            document.getElementById('addExamForm').reset();
            fetchAndRenderExams(); // Reload calendar data
        } else {
            const err = await response.text();
            alert('Failed to schedule exam: ' + err);
        }
    })
    .catch(error => {
        console.error('Error scheduling exam:', error);
        alert('An error occurred. Please try again.');
    });
}

// Month Navigation
function prevMonth() {
    currentMonth--;
    if (currentMonth < 0) {
        currentMonth = 11;
        currentYear--;
    }
    renderCalendar(currentMonth, currentYear);
}

// Month Navigation
function nextMonth() {
    currentMonth++;
    if (currentMonth > 11) {
        currentMonth = 0;
        currentYear++;
    }
    renderCalendar(currentMonth, currentYear);
}

// Initialize calendar on page load
document.addEventListener('DOMContentLoaded', () => {
    const calendarDays = document.getElementById('calendarDays');
    if (calendarDays) {
        // Fetch and draw
        fetchAndRenderExams();

        // Bind nav buttons
        document.getElementById('prevMonthBtn').addEventListener('click', prevMonth);
        document.getElementById('nextMonthBtn').addEventListener('click', nextMonth);
        
        // Bind form submit
        const addExamForm = document.getElementById('addExamForm');
        if (addExamForm) {
            addExamForm.addEventListener('submit', submitAddExam);
        }
    }
});
