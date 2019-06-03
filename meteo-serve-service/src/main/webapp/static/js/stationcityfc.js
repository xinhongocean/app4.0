/**
 * Created by liuxiaochang on 2016/3/11.
 */
$('#btncommit_search').unbind('click');
$("#btncommit_search").on('click', function () {
    var cname = '';
    var lat = '';
    var lng = '';
    var year = '';
    var month = '';
    var day = '';
    var hour = '';
    var elem = '';
    if ($("#cname_input").val()) {
        cname = $("#cname_input").val();
    }
    if ($("#lat_input").val()) {
        lat = $("#lat_input").val();
    }
    if ($("#lng_input").val()) {
        lng = $("#lng_input").val();
    }
    if ($("#year_input").val()) {
        year = $("#year_input").val();
    }
    if ($("#month_input").val()) {
        month = $("#month_input").val();
    }
    if ($("#day_input").val()) {
        day = $("#day_input").val();
    }
    if ($("#hour_input").val()) {
        hour = $("#hour_input").val();
    }
    if ($("#elem_input").val()) {
        elem = $("#elem_input").val();
    }
    var url = "/stationdata_cityfc/";
    if (cname != '') {
        url = url + "datafromcname";
    } else {
        url = url + "datafromlatlng";
    }
    jQuery.ajax({
        type: "post",
        url: url,
        dataType: "json",
        //contentType : "application/json; charset=utf-8",
        cache: false,
        data: {
            "cname": cname,
            "lat": lat,
            "lng": lng,
            "year": year,
            "month": month,
            "day": day,
            "hour": hour,
            "elem": elem
        },
        success: function (resdata, textStatus, jqXHR) {
            $('#search_result').empty();
            if (resdata == undefined || resdata == null) {
                $('#search_result').val('HTTP请求无数据返回!');
                return;
            }
            $('#search_result').html(resdata.status_code + "<br>"
                + resdata.status_msg + "<br>"
                + resdata.station_cname + "<br>"
                + resdata.station_code + "<br>"
                + resdata.time + "(UTC)<br>"
                + resdata.lat + "," + resdata.lng + "<br>"
                + resdata.data);
        },
        beforeSubmit: function (jqXHR, settings) {
            //    $.blockUI({ message: "<h3><img src='/static/images/loading_32.gif' />正在处理,请稍后...</h3>"});
        },
        complete: function (jqXHR, textStatus) {
            //   $.unblockUI();
        },
        error: function (jqXHR, textStatus) {
        }
    });
});