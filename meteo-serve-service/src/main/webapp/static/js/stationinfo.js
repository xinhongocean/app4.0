/**
 * Created by liuxiaochang on 2016/1/13.
 */
//新增四级地址
$('#btncommit_search').unbind('click');
$("#btncommit_search").on('click', function () {
    var py = '';
    var cname = '';
    var lat = '';
    var lng = '';
    if ($("#py_input").val()) {
        py = $("#py_input").val();
    }
    if ($("#cname_input").val()) {
        cname = $("#cname_input").val();
    }
    if ($("#lat_input").val()) {
        lat = $("#lat_input").val();
    }
    if ($("#lng_input").val()) {
        lng = $("#lng_input").val();
    }
    jQuery.ajax({
        type:      "post",
        url:       "/station/surfinfopyorcnamejson.do",
        dataType : "json",
        //contentType : "application/json; charset=utf-8",
        cache : false,
        //data : JSON.stringify({
        //    "py": py,
        //    "cname": cname}),
        data : {
            "py": py,
            "cname": cname},
        success: function (data, textStatus, jqXHR) {
            $('#addjddist_result').empty();
            alert(data);
            if (data == undefined || data == null) {
                $('#sationinfo_search_result').val('HTTP请求无数据返回!');
                return;
            }
            //data = $.parseJSON(data);
            //if(typeof data == 'string'){
            //    data = $.parseJSON(data);
            //} else {
            //    alert(data);
            //}
            $('#sationinfo_search_result').html(data.stationinfolist);
         },
        beforeSubmit: function (jqXHR, settings) {
             //    $.blockUI({ message: "<h3><img src='/static/images/loading_32.gif' />正在处理,请稍后...</h3>"});
        },
        complete: function (jqXHR, textStatus) {
             //   $.unblockUI();
        },
        error: function(jqXHR, textStatus) {
        }
    });
});

//$.post("/edit/addjddist.do",
//    { "jd_distname": jd_distname,
//        "jd_distlevel":jd_distname,
//      "jd_parentcode": jd_parentcode,
//        "na_distcodelist":na_distcodelist},
//    function(data) {
//        $('#addjddist_result').empty();
//        if(data == undefined || data == null){
//            alert("HTTP请求无数据返回！");
//            $('#addjddist_result').val("HTTP请求无数据返回!");
//            return;
//        }
//        if (data.status == 0) {
//            $('#addjddist_result').val("新增成功!");
//        } else {
//            $('#addjddist_result').val(data.message);
//        }
//    });
//});
