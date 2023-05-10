
var loader = '<div style="text-align:center"><div style="width:8rem;height:8rem;" class="spinner-border text-dark me-2" role="status"><span class="visually-hidden">Loading...</span></div></div>'
var ref = "";

$(".config-user-access").each(function (e) {
    var elt = $(this);
    elt.click(function (e) {
        e.preventDefault();
        $.ajax({
            url: $(elt).prop("href"),
            type: "GET",
            dataType: "JSON",
            success: function (response) {
                $("#modalRoleUser .modal-body").html(response.data);
            },
            error: function () {
                showNotif("Erreur 500 ! Une erreur est survenue !", "danger");
            }
        })
    })
})

$("form#importFileForm").on("submit", function (e) {
    e.preventDefault();
    var elt = $(this);
    var $form = $(this)[0];
    var data = new FormData($form);
    $("#submitButton").prop("disabled", true);
    $.ajax({
        url: elt.attr("action"),
        type: elt.attr("method"),
        enctype: 'multipart/form-data',
        data: data,
        processData: false,
        contentType: false,
        cache: false,
        timeout: 1000000,
        beforeSend: function () {
            $('#resultShow').trigger("click");
            $('#results .modal-body').html(loader)
        },
        success: function (response) {
            $("#submitButton").prop("disabled", false);
            $form.reset();
            if (response.isOk) {
                $('#results .modal-body').html(response.results)
                showNotif("Mises en demeure envoyées !", "success", 10000);
            } else {
                showNotif("Impossible d'envoyer les mails. Verifiez votre fichier", "danger", 10000);
                $("#results #closeModal").trigger("click");
                $('#results .modal-body').html("");
            }
        },
        error: function (jqXHR, textStatus, errorThrown) {
            $("#submitButton").prop("disabled", false);
            showNotif("Erreur 500 ! Une erreur est survenue !", "danger", 10000);
            $('#results .modal-body').html("ERREUR 500 !")
        }
    });
})

$(".inputRole").each(function (e) {
    var elt = $(this);
    alert();
    elt.click(function () {
        if (elt.find('input').is(':checked')) {
            $('input.roleAdmin').prop('checked', true);
        }
    })
})

$("#typeDomiciliation").on("change", function (e) {
    var val = $(this).val();
    var type = "importation";
    if (val == 0) {
        type = "exportation";
    }
    $.ajax({
        url: "/rest-get-type-transactions/" + type,
        dataType: "JSON",
        beforeSend: function () {
            $("#typesTransactions").prop("disable", true).html("");
        },
        success: function (response) {
            if (response.hasData) {
                $("#typesTransactions").prop("disable", false).html(response.data);
            } else {
                $("#typesTransactions").prop("disable", true).html("");
                showNotif("Aucun type de transaction trouvé pour le type de domiciliation selectionner", "danger");
            }
        },
        error: function () {
            $("#typesTransactions").prop("disable", true).html("");
            showNotif("Erreur 500 . Une erreur est survenue !", "danger");
        }
    })
})

$("#modalRoleUser form").on("submit", function (e) {
    e.preventDefault();
    var form = $(this);
    $.ajax({
        url: form.prop("action"),
        type: form.prop("method"),
        dataType: "JSON",
        data: form.serialize(),
        success: function (response) {
            if (response.isOk) {
                $("#modalRoleUser").modal("hide");
                showNotif("Les access ont été configuré", 10000);
            setTimeout(window.location.href = '/admin-users-list', 50000);
            } else {
                showNotif("Impossible de configurer les access", "danger", 10000);
                setTimeout(window.location.href = '/admin-users-list', 40000);
            }
        },
        error: function () {
            showNotif("Erreur 500 ! Une erreur est survenue !", "danger", 10000);
            setTimeout(window.location.href = '/admin-users-list', 40000);
        }
    })
})

$(".fichiers-manquants").each(function () {
    var elt = $(this);
    elt.click(function (e) {
        $.ajax({
            url: "/" + elt.data("ref") + "-transaction/fichiers-manquants",
            type: "GET",
            dataType: "JSON",
            success: function (response) {
                $("#fichiers-manquants .modal-body p").html(response.response);
            },
            error: function () {
                showNotif("Erreur 500 ! Une erreur est survenue !", "danger", 10000);

            }
        })
    })
})

$('.ajaxHref').on("click", function (e) {
    e.preventDefault();
    let $elt = $(this);
    madeAction($elt);
})

function madeAction($elt) {
    $.ajax({
        url: $elt.prop("href"),
        type: "GET",
        dataType: "JSON",
        success: function (response) {
            if (response.isOk) {
                showNotif(response.message)
            } else {
                showNotif(response.message, "danger");
            }
        },
        error: function () {
            showNotif("Erreur 500 ! Une erreur est survenue !", "danger");
        }
    })
}

function showNotif(contenu, type = 'success', timer = 5000) {
    var rc = 'danger';
    if (type == 'danger') {
        rc = 'success';
    }
    $('#type').removeClass('bg-' + rc);
    $('#notif').addClass('bg-' + type);
    $('#notif p').html(contenu);
    $("#notif").fadeIn(500);
    setTimeout(function () {
        $("#notif").fadeOut(500);
    }, timer);
}


// ====================================================================================

$("form#apurementForm").on("submit", function (e) {
    e.preventDefault();
    var elt = $(this);
    var $form = $(this)[0];
    var data = new FormData($form);
    $("#apSubmitButton").prop("disabled", true);
    $.ajax({
        url: elt.attr("action"),
        type: elt.attr("method"),
        enctype: 'multipart/form-data',
        data: data,
        processData: false,
        contentType: false,
        cache: false,
        timeout: 1000000,
        beforeSend: function () {
            $('#resultShow').trigger("click");
            $('#miseDemeureResults .modal-body').html(loader)
        },
        success: function (response) {
            $("#apSubmitButton").prop("disabled", false);
            $form.reset();
            if (response.isOk) {
                $('#miseDemeureResults .modal-body').html(response.results)
                showNotif("Mises en demeure envoyées !", "success", 10000);
            } else {
                showNotif("Impossible d'envoyer les mails. Verifiez votre fichier", "danger", 10000);
                $("#miseDemeureResults #closeModal").trigger("click");
                $('#results .modal-body').html("");
            }
        },
        error: function (jqXHR, textStatus, errorThrown) {
            $("#apSubmitButton").prop("disabled", false);
            showNotif("Erreur 500 ! Une erreur est survenue !", "danger", 10000);
            $('#results .modal-body').html("ERREUR 500 !")
        }
    });
})

$(".apurement-btn-action").each(function () {
    var elt = $(this);
    elt.on("click", function (e) {
        e.preventDefault();
        ref = elt.data("ref");
        $("#modal").attr("data-ref", ref);
        $("#modal ul li a").removeClass("active");
        $('#fichiersManquant').addClass("active")
        setTimeout(function(){
            $('#fichiersManquant').trigger("click");
        }, 1000)
        $("#modal").modal("show");
    });
})

$('#fichiersManquant').on("click", function (e) {
    // var ref = $("#modal").data("ref");
    $("#modal .tab-title").html("Liste des fichiers manquants");
    $.ajax({
        url: "/rest-apurements/" + ref + "/fichiers-manquants",
        type: "GET",
        dataType: "JSON",
        success: function (response) {
            if (response.hasData) {
                $("#waiting-files-list").html(response.data);
            } else {
                $("#waiting-files-list").html("Aucun fichier manquant trouvé !");
            }
        },
        error: function () {
            showNotif("Erreur 500 ! Une erreur est survenue !", "danger");
        }
    })
})

$('#showFileForms').on("click", function (e) {
    // var ref = $("#modal").data("ref");
    $("#modal .tab-title").html("Formulaire pour déposer les fichiers manquants");
    $.ajax({
        url: "/rest-apurements/" + ref + "/files-forms",
        type: "GET",
        dataType: "JSON",
        success: function (response) {
            if (response.hasData) {
                $("#waiting-files-list").html(response.data);
                $("#addFilesButton").on("click" ,function(e) {
                    uploadFiles();
                });
            } else {
                $("#waiting-files-list").html("Aucun fichier manquant trouvé !");
            }
        },
        error: function () {
            showNotif("Erreur 500 ! Une erreur est survenue !", "danger");
        }
    })
})

$("#validerApurement").on("click", function (e) {
    e.preventDefault();
    $.ajax({
        url: "/rest-apurements/" + ref + "/apurer",
        dataType: "JSON",
        success: function (response) {
            var type = 'danger';
            if (response.isOk) {
                type = 'success';
                $("#validerApurement").fadeOut(300);
            }
            showNotif(response.message, type, 8000);
        },
        error: function () {
            showNotif("Erreur 500 ! Une erreur est survenue !", "danger");
        }
    })
})

function uploadFiles() {

    $("#result").html("");
    $("#vertical-icon-tab-2 form").each(function() {
        var elt = $(this);
        var form = $(this)[0];
        var data = new FormData(form);
        $("#addFilesButton").prop("disabled", true);
        $.ajax({
            type: "POST",
            enctype: 'multipart/form-data',
            url: $(elt).attr("action"),
            data: data,
            // prevent jQuery from automatically transforming the data into a query string
            processData: false,
            contentType: false,
            cache: false,
            timeout: 1000000,
            success: function (response) {
                $("#addFilesButton").prop("disabled", false);
                $(elt)[0].reset();
                if (response.isUpload) {

                    $("#result").append("<span class='text-success'>"+response.message+"</span><br>");
                }
                else {
                    $("#result").append("<span class='text-danger'>"+response.message+"</span><br>");
                }
            },
            error: function (jqXHR, textStatus, errorThrown) {
                $("#result").html(jqXHR.responseText);
                console.log("ERROR : ", jqXHR.responseText);
                $("#addFilesButton").prop("disabled", false);
            }
        })
    })
}

$('#filesSend').on("click", function (e) {
    // var ref = $("#modal").data("ref");
    $("#modal .tab-title").html("Fichiers déposés pour apurements");
    $.ajax({
        url: "/rest-apurements/" + ref + "/files",
        type: "GET",
        dataType: "JSON",
        success: function (response) {
            if (response.hasData) {
                $("#waiting-files-list").html(response.data);
                $("p.fichier-apurement a.valider").on("click", function (ev) {
                    ev.preventDefault();
                    validerFichier($(this));
                })
                validerFichier();
            } else {
                $("#waiting-files-list").html("Aucun fichier manquant trouvé !");
            }
        },
        error: function () {
            showNotif("Erreur 500 ! Une erreur est survenue !", "danger");
        }
    })
})

function montant_format(number) {
    var decimals = 2;
    var dec_point = '.';
    var thousands_sep = ',';
    number = (number + '')
            .replace(/[^0-9+\-Ee.]/g, '');
    var n = !isFinite(+number) ? 0 : +number,
            prec = !isFinite(+decimals) ? 0 : Math.abs(decimals),
            sep = (typeof thousands_sep === 'undefined') ? ',' : thousands_sep,
            dec = (typeof dec_point === 'undefined') ? '.' : dec_point,
            s = '',
            toFixedFix = function (n, prec) {
                var k = Math.pow(10, prec);
                return '' + (Math.round(n * k) / k)
                        .toFixed(prec);
            };
    // Fix for IE parseFloat(0.55).toFixed(0) = 0;
    s = (prec ? toFixedFix(n, prec) : '' + Math.round(n))
            .split('.');
    if (s[0].length > 3) {
        s[0] = s[0].replace(/\B(?=(?:\d{3})+(?!\d))/g, sep);
    }
    if ((s[1] || '')
            .length < prec) {
        s[1] = s[1] || '';
        s[1] += new Array(prec - s[1].length + 1)
                .join('0');
    }
    return s.join(dec);
}

function validerFichier(elt) {
    $.ajax({
        url: elt.prop("href"),
        dataType: "JSON",
        success: function (response) {
            var type = 'danger';
            if (response.isOk) {
                type = 'success';
                elt.html(response.fa);
                elt.attr("title", response.title);
                elt.attr("href", response.href);
            }
            showNotif(response.message, type);
        },
        error: function () {
            showNotif("Erreur 500 ! Une erreur est survenue !", "danger");
        }
    })
}

var tmpDate;
$('.dateExpirationApurement').each(function () {
    var td = $(this);
    var data = $(this).attr("data");
    td.dblclick(function () {
        var oldDate = td.data("value");
        if (oldDate != "") {
            tmpDate = oldDate;
        }
        var inputDate = "<input min='" + tmpDate + "' type='date' value='" + tmpDate + "'>";
        td.html(inputDate);
        td.find('input').focus();
        td.find('input').trigger("click");
        td.find('input').blur(function () {
            var newDate = td.find('input').val();
            if (newDate == "" || newDate == oldDate) {
                newDate = oldDate;
                td.html(newDate);
            } else {
                changeApurementExpirationDate(data, newDate, td);
            }
        })
    })

})

function changeApurementExpirationDate(data, newDate, elt) {
    $.ajax({
        url: "/change-expiration-apurement-date/" + data + "/" + newDate,
        success: function (response) {
            if (response.isChanged) {
                elt.html(response.dateText)
                elt.data("value", response.dataValue)
                // elt.prev().prev().prev().text(response.nbJours)
            } else {
                elt.html(response.delay);
                elt.trigger("dblclick");
                showNotif(response.errorMessage, 'danger', 7000)
            }
        },
        error: function () {
            showNotif("Impossible de modifier la date ! erreur 500", 'danger', 7000)
            elt.html(tmpDate);
            elt.trigger("dblclick");
        }
    })
}

$("#isForImportationLabel").on("click", function () {
    if ($(this).find("input").is(":checked")) {
        $("#type").prop("disabled", false);
    } else {
        $("#type").prop("disabled", true);
    }
})
'use strict';

var singleUploadForm = document.querySelector('#singleUploadForm');
var singleFileUploadInput = document.querySelector('#singleFileUploadInput');
var singleFileUploadError = document.querySelector('#singleFileUploadError');
var singleFileUploadSuccess = document.querySelector('#singleFileUploadSuccess');

var multipleUploadForm = document.querySelector('#multipleUploadForm');
var multipleFileUploadInput = document.querySelector('#multipleFileUploadInput');
var multipleFileUploadError = document.querySelector('#multipleFileUploadError');
var multipleFileUploadSuccess = document.querySelector('#multipleFileUploadSuccess');

function uploadSingleFile(file) {
    var formData = new FormData();
    formData.append("file", file);

    var xhr = new XMLHttpRequest();
    xhr.open("POST", "/uploadFile");

    xhr.onload = function() {
        console.log(xhr.responseText);
        var response = JSON.parse(xhr.responseText);
        if(xhr.status == 200) {
            singleFileUploadError.style.display = "none";
            singleFileUploadSuccess.innerHTML = "<p>File Uploaded Successfully.</p><p>DownloadUrl : <a href='" + response.fileDownloadUri + "' target='_blank'>" + response.fileDownloadUri + "</a></p>";
            singleFileUploadSuccess.style.display = "block";
        } else {
            singleFileUploadSuccess.style.display = "none";
            singleFileUploadError.innerHTML = (response && response.message) || "Some Error Occurred";
        }
    }

    xhr.send(formData);
}

function uploadMultipleFiles(files) {
    var formData = new FormData();
    for(var index = 0; index < files.length; index++) {
        formData.append("files", files[index]);
    }

    var xhr = new XMLHttpRequest();
    xhr.open("POST", "/uploadMultipleFiles");

    xhr.onload = function() {
        console.log(xhr.responseText);
        var response = JSON.parse(xhr.responseText);
        if(xhr.status == 200) {
            multipleFileUploadError.style.display = "none";
            var content = "<p>All Files Uploaded Successfully</p>";
            for(var i = 0; i < response.length; i++) {
                content += "<p>DownloadUrl : <a href='" + response[i].fileDownloadUri + "' target='_blank'>" + response[i].fileDownloadUri + "</a></p>";
            }
            multipleFileUploadSuccess.innerHTML = content;
            multipleFileUploadSuccess.style.display = "block";
        } else {
            multipleFileUploadSuccess.style.display = "none";
            multipleFileUploadError.innerHTML = (response && response.message) || "Some Error Occurred";
        }
    }

    xhr.send(formData);
}

singleUploadForm.addEventListener('submit', function(event){
    var files = singleFileUploadInput.files;
    if(files.length === 0) {
        singleFileUploadError.innerHTML = "Please select a file";
        singleFileUploadError.style.display = "block";
    }
    uploadSingleFile(files[0]);
    event.preventDefault();
}, true);

multipleUploadForm.addEventListener('submit', function(event){
    var files = multipleFileUploadInput.files;
    if(files.length === 0) {
        multipleFileUploadError.innerHTML = "Please select at least one file";
        multipleFileUploadError.style.display = "block";
    }
    uploadMultipleFiles(files);
    event.preventDefault();
}, true);

