function confirmDelete() {
    var r = confirm("Are you sure you want to delete this user?"));
      if (r == true) {
        return true;
    } else {
        return false;
    }
  }


  function confirmAdmin() {
    var r = confirm("Are you sure you want to grant admin rights?");
    if (r == true) {
        return true;
    } else {
        return false;
    }
}
  

  