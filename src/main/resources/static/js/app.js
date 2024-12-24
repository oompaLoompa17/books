function addToRead(bookId) {
    const userId = document.querySelector('#userId').value;
    fetch(`/books/to-read/${bookId}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ userId: userId }),
    })
        .then(response => {
            if (response.ok) {
                return response.text();
            } else if (response.status === 409) {
                throw new Error("Book is already in your 'To Read' or 'Read' list.");
            } else {
                throw new Error('An error occurred while adding the book.');
            }
        })
        .then(message => {
            const feedback = document.getElementById('feedback');
            feedback.style.color = 'green';
            feedback.style.display = 'block';
            feedback.textContent = message;
        })
        .catch(error => {
            const feedback = document.getElementById('feedback');
            feedback.style.color = 'red';
            feedback.style.display = 'block';
            feedback.textContent = error.message;
        });
}

function markAsRead(bookId) {
    fetch(`/books/mark-as-read/${bookId}`, {
        method: 'POST',
    })
        .then(response => {
            if (response.ok) {
                return response.text();
            } else {
                throw new Error('An error occurred while marking the book as read.');
            }
        })
        .then(message => {
            alert(message); // Display success message
            window.location.reload(); // Reload the page to reflect changes
        })
        .catch(error => {
            alert(error.message); // Display error message
        });
}