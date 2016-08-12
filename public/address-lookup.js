$(document).ready(function() {
$("#myForm").find("#hiddenselection").prop('checked', false);
    $('#editAddress').click(function(){
        $("#myForm").find("#hiddenselection").prop('checked', true);
        $("#myForm").submit();
        return false;
    });
});