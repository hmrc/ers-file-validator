 $(document).ready(function() {
    var $radio      = $('input:radio'), // cache all radio buttons
        $checkOther = $('input:checkbox[value=99]'), // cache all 'other' checkboxes, these will always have a value of 99
        $checkAll   = $('label:contains("All") input:checkbox'), // cache all checkboxes with label all
        $checks     = $('label input:checkbox:not(label input:checkbox[value=99]):not(label:contains("All") input:checkbox)'); // cache all the checkboxes that are not the previous two

    function showHide(radio) {
        if (radio.indexOf(".") >= 0){
            radio = radio.replace(".", "\\.");
        }
        // if all radios are unchecked, hide all content/headings shown based on checked radios
        if ($('#' + radio + '-yes').not(':checked') && $('#' + radio + '-no').not(':checked')) {
            if ($('#' + radio + '-yes-content') !== undefined) {
                $('#' + radio + '-yes-content').hide();
            }
            if ($('#' + radio + '-no-content') !== undefined) {
                $('#' + radio + '-no-content').hide();
            }
            if (radio == 'alcoholSupplier' && $('#supplier') !== undefined) {
                $('#supplier').hide();
            }
        }
        // show content for yes radios, hide content for no
        if ($('#' + radio + '-yes').is(':checked')) {
            if ($('#' + radio + '-yes-content') !== undefined) {
                $('#' + radio + '-yes-content').show();
            }
            if ($('#' + radio + '-no-content') !== undefined) {
                $('#' + radio + '-no-content').hide().find('input:text').val('');
                $('#' + radio + '-no-content').find('input:radio, input:checkbox').attr('checked', false);
            }
            if (radio == 'alcoholSupplier' && $('#supplier') !== undefined) {
                $('#supplier').show();
            }
        }
        // show content for no radios, hide content for yes
        if ($('#' + radio + '-no').is(':checked')) {
            if ($('#' + radio + '-no-content') !== undefined) {
                $('#' + radio + '-no-content').show();
            }
            if ($('#' + radio + '-yes-content') !== undefined) {
                $('#' + radio + '-yes-content').hide().find('input:text').val('');
                $('#' + radio + '-yes-content').find('input:radio, input:checkbox').attr('checked', false);
            }
            if (radio == 'alcoholSupplier' && $('#supplier') !== undefined) {
                $('#supplier').hide();
            }
        }
    }

    function showHideOther(checkbox){
       if ($('#' + checkbox).is(':checked')) {
           $('#other-' + checkbox.replace('_99', '')).show();
       } else {
           $('#other-' + checkbox.replace('_99', '')).hide();
           $('#other-' + checkbox.replace('_99', '') + ' input').val('');
       }
    }

    function checkAll(checkbox, name) {
        $(checkbox).is(':checked') ?
            $checks.filter('[name="'+ name + '"]').prop('checked', true) :
            $checks.filter('[name="'+ name + '"]').prop('checked', false);
    }

    // check/uncheck 'all' checkbox if all other checkboxes are checked/unchecked
    function checks(name) {
        $checks.filter('[name="'+ name + '"]').length == $checks.filter('[name="'+ name + '"]').filter(':checked').length ?
            $checkAll.filter('[name="'+ name + '"]').prop('checked', true) :
            $checkAll.filter('[name="'+ name + '"]').prop('checked', false);
    }

    // each function to read radio buttons and checkboxes on load, change function to track changes
    $radio.each(function() {
        showHide(this.name);
    });
    $radio.on('change', function() {
        showHide(this.name);
    });

    $checkOther.each(function() {
        showHideOther(this.id);
    });

    $checkOther.on('change', function() {
        showHideOther(this.id);
    });

    $checkAll.on('change', function() {
        checkAll(this, this.name);
    });

    $checks.each(function() {
        checks();
    });
    $checks.on('change', function() {
        checks(this.name);
    });
});
