// function addToRead(bookId) {
//     console.log("Adding Book ID to Read List: ", bookId); // Debugging log
//     fetch(`/books/to-read/${bookId}`, {
//         method: 'POST',
//         headers: {
//             'Content-Type': 'application/json',
//         },
//     })
//         .then(response => {
//             if (response.ok) {
//                 return response.text();
//             } else if (response.status === 409) {
//                 throw new Error("Book is already in your 'To Read' or 'Read' list.");
//             } else if (response.status === 401) {
//                 throw new Error("You must be logged in to add books.");
//             } else {
//                 throw new Error('An error occurred while adding the book.');
//             }
//         })
//         .then(message => {
//             const feedback = document.getElementById('feedback');
//             feedback.style.color = 'green';
//             feedback.style.display = 'block';
//             feedback.textContent = message;
//         })
//         .catch(error => {
//             const feedback = document.getElementById('feedback');
//             feedback.style.color = 'red';
//             feedback.style.display = 'block';
//             feedback.textContent = error.message;
//         });
// }

function addToRead(bookId) {
    fetch(`/books/to-read/${bookId}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
    })
        .then(response => response.text().then(message => ({ status: response.status, message })))
        .then(({ status, message }) => {
            const feedback = document.querySelector(`.feedback-message[data-book-id="${bookId}"]`);
            feedback.style.display = 'block';
            feedback.style.color = status === 200 ? 'green' : 'red';
            feedback.textContent = message;
        })
        .catch(error => {
            const feedback = document.querySelector(`.feedback-message[data-book-id="${bookId}"]`);
            feedback.style.display = 'block';
            feedback.style.color = 'red';
            feedback.textContent = 'An unexpected error occurred.';
        });
}

function removeFromToRead(bookId) {
    fetch(`/books/remove-to-read/${bookId}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
    })
        .then(response => response.text().then(message => ({ status: response.status, message })))
        .then(({ status, message }) => {
            if (status === 200) {
                alert(message); // Show success message
                window.location.reload(); // Reload the dashboard
            } else {
                alert(message); // Show error message
            }
        })
        .catch(error => {
            alert('An error occurred while removing the book.');
        });
}

document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll('.remove-from-read').forEach(button => {
        button.addEventListener('click', event => {
            const bookId = event.target.getAttribute('data-book-id');
            removeFromToRead(bookId);
        });
    });
});



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

document.addEventListener('DOMContentLoaded', () => {
    // Add event listeners to "Add to Read" buttons
    document.querySelectorAll('.add-to-read').forEach(button => {
        button.addEventListener('click', event => {
            const bookId = event.target.getAttribute('data-book-id');
            console.log("Adding Book ID to Read List: ", bookId); // Debugging log
            addToRead(bookId);
        });
    });
});



