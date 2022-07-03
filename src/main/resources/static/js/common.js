function checkNotNull(str) {
    if (str == null || str == "" || str.length < 1 || str == undefined) {
        return false;
    }
    return true;
}

function zuiMsg(msg){
    new $.zui.Messager(msg, {
        type: 'warning',
        placement: 'center'
    }).show();
}

function zuiSuccessMsg(msg){
    new $.zui.Messager(msg, {
        type: 'success',
        placement: 'center'
    }).show();
}
