<#include "./top.ftl">
<nav class="navbar navbar-default" role="navigation">
    <div class="container-fluid">
        <div class="navbar-header">
            <button type="button" class="navbar-toggle" data-toggle="collapse"
                    data-target=".navbar-collapse-example">
                <span class="sr-only">切换导航</span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
            </button>
            <a class="navbar-brand" href="/" target="_blank"><i class="icon-home"></i> 首页</a>
        </div>
        <div class="collapse navbar-collapse navbar-collapse-example">
            <ul class="nav navbar-nav">
                <li><a href="/myblog/"><i class="icon-list-ol"></i> 基础数据</a></li>
                <li class="dropdown">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown"><i class="icon-group"></i> 用户管理<b
                                class="caret"></b></a>
                    <ul class="dropdown-menu" role="menu">
                        <li><a href="/myblog/user/list">用户列表</a></li>
                    </ul>
                </li>
                <li class="dropdown">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown"><i class="icon-th-list"></i> 文章管理<b
                                class="caret"></b></a>
                    <ul class="dropdown-menu" role="menu">
                        <li><a href="/myblog/article/type/list">文章类型</a></li>
                        <li><a href="/myblog/article/tag/list">文章标签</a></li>
                        <li><a href="/myblog/article/list">文章管理</a></li>
                    </ul>
                </li>
                <li><a href="/myblog/ad/list"><i class="icon-dollar"></i> 广告管理</a></li>
                <li><a href="/myblog/link/list"><i class="icon-smile"></i>友情连接</a></li>

            </ul>
            <ul class="nav navbar-nav navbar-right">
                <li class="dropdown">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown"><i class="icon-th-list"></i> 系统<b
                                class="caret"></b></a>
                    <ul class="dropdown-menu" role="menu">
                        <li><a href="javascript:void(0)" data-toggle="modal" data-target="#updatePasswordModal"> <i class="icon-edit"></i> 修改密码</a></li>
                        <li><a href="/myblog/logout"><i class="icon-signout"></i> 退出登陆</a></li>
                    </ul>
                </li>

            </ul>
        </div>
    </div>
</nav>

<div class="modal fade" id="updatePasswordModal">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal"><span aria-hidden="true">×</span><span class="sr-only">关闭</span></button>
                <h4 class="modal-title">修改管理员密码</h4>
            </div>
            <div class="modal-body">
                <form class="form-horizontal">
                    <div class="form-group">
                        <label for="newPassword" class="col-sm-2">新密码</label>
                        <div class="col-md-6 col-sm-10">
                            <input type="text" class="form-control" name="newPassword" id="newPassword" placeholder="新密码">
                        </div>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">关闭</button>
                <button type="button" class="btn btn-primary" onclick="updatePassword()">确定</button>
            </div>
        </div>
    </div>
</div>
<script>


    function updatePassword() {
        $.post("/myblog/password/update", {
                newPassword: $("#newPassword").val()
            },
            function (data) {
                if (data.code == 200) {
                    alert(data.message)
                    location.reload();
                    return;
                }
                zuiMsg(data.message);
            });
    }


</script>