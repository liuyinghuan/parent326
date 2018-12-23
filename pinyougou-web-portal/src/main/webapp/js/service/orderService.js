//购物车服务层
app.service('orderService',function($http){


    this.submitOrder=function (order) {
        //alert('111')
       return $http.post("/order/add.do",order);
    }


});