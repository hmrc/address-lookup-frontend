$(document).ready(function () {

    $('a.maplink').each(function(i) {
        var a = $(this);
        var loc = a.data('loc');
        if (loc) {
            var href = 'https://www.google.com/maps/@' + loc + ',18z';
            a.attr('href', href).attr('target', '_blank');
            console.log(href);
        } else {
            console.log('SKIPPED');
        }
    });

});
