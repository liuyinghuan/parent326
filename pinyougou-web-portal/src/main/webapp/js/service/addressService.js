//购物车服务层
app.service('addressService',function($http){


    this.findListByLoginUser=function () {
       return $http.get("/address/findListByLoginUser.do");
    }


});