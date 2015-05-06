# 提示校验失败的话需要修改sudoers配置, 注释掉 Defaults    requiretty项
svn up
svn add *
svn commit --username hawk.gtci --password hawk.dai -m CICommit
