//购物车服务层
app.service('cartService',function($http){

    //存购物车
    this.setCartList=function (cartList) {
        localStorage.setItem("cartList",JSON.stringify(cartList));
    }

    //取购物车
    this.getCartList=function () {
        var cartListStr= localStorage.getItem("cartList");
        if(cartListStr==null){
            return [];
        }else{
            return JSON.parse(cartListStr);
        }
    }


    //清除购物车
    this.removeCartList=function () {
        localStorage.removeItem("cartList");

    }


    //查询购物车
    this.findCartList=function (cartList) {

        return $http.post("cart/findCartList.do",cartList);
    }

    //添加商品到购物车
    this.addGoodsToCartList=function (cartList,itemId,num) {
        //alert("111");
        return  $http.post("cart/addGoodsToCartList.do?itemId="+itemId+"&num="+num ,cartList);
    }

    //合计数
    this.sum=function (cartList) {
        var totalValue={totalNum:0,totalMoney:0};
        for(var i=0;i<cartList.length;i++){
            var cart = cartList[i];
            for(var j=0;j< cart.orderItemList.length;j++ )  {
                var orderItem= cart.orderItemList[j];
                totalValue.totalNum+=orderItem.num;//数量累加
                totalValue.totalMoney+=orderItem.totalFee;//金额累加
            }
        }
        return totalValue;
    }

});