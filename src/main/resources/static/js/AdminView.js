function confirmDelete() {
    if (confirm("Are you sure you want to delete this user?")) {
      // User confirmed, submit the form
      document.getElementById("deleteForm").submit();
    } else {
      // User cancelled, do nothing
    }
  }

  function confirmAdmin() {
    if (confirm("Are you sure you want to grant admin this user?")) {
      // User confirmed, submit the form
      document.getElementById("grantAdmin").submit();
    } else {
      // User cancelled, do nothing
    }
  }