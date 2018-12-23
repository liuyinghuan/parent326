//购物车控制层
app.controller('cartController',function($scope,$location,cartService,addressService,orderService){


    $scope.loginname="";

    //页面初始化
    $scope.init=function () {
        $scope.cartList= cartService.getCartList();
        var itemId= $location.search()["itemId"];//商品ID
        var num= $location.search()["num"]; // 数量
        if(itemId!=null && num!=null){
            $scope.addGoodsToCartList(itemId,num);//添加商品到购物车
        }else {
            //如果没添加,就去查找购物车
            $scope.findCartList();
        }

    }

    $scope.cartList=[];

    //查询购物车
    $scope.findCartList=function () {
        $scope.cartList= cartService.getCartList();
        cartService.findCartList($scope.cartList).success(function (response) {
            $scope.cartList=response.data;
            //如果用户已经登录,清除本地购物车
            if (""!=response.loginname){
                //alert("qingchu")
                cartService.removeCartList();
            }
            $scope.loginname=response.loginname;
        })
    }

    //添加购物车
    $scope.addGoodsToCartList=function (itemId,num) {
        cartService.addGoodsToCartList( cartService.getCartList() ,itemId, num ).success(
            function (response) {
                if(response.success){

                    $scope.cartList=response.data;

                    if (""!=response.loginname){
                        //alert("qingchu")
                        $scope.loginname=response.loginname;
                        cartService.removeCartList();//清除购物车
                    }else {
                        cartService.setCartList(response.data);//保存购物车
                    }


                }
            }
        )
    }

    //当购物车发生变化，计算合计
    $scope.$watch("cartList",function (newValue,oldValue) {
        $scope.totalValue=  cartService.sum($scope.cartList);

    })


    //查询当前用户的地址列表
    $scope.findListByLoginUser=function () {
        //alert(111)
        addressService.findListByLoginUser().success(function (response) {

            $scope.addressList=response;

            //默认地址选择
            for (var i=0;i<$scope.addressList.length;i++){
                if ($scope.addressList[i].isDefault=='1'){
                    $scope.address=$scope.addressList[i];
                    break;
                }
            }

        })

    }

    //选择地址
    $scope.selectAddress=function (address) {
        //alert("111")
        $scope.address=address;
    }

    //判断地址是否是当前地址
    $scope.isAddress=function (address) {
        if ($scope.address==address){
            return true;
        }else {
            return false;
        }
    }


    //选择支付方式
    $scope.order={paymentType:"1"} ;
    $scope.selectPaymentType=function (type) {
        $scope.order.paymentType=type;
    }
    
    $scope.submitOrder=function () {
        //收货人信息
        //alert($scope.address.address)
        $scope.order.receiverAreaName=$scope.address.address;//地址
        $scope.order.receiverMobile=$scope.address.mobile;//电话
        $scope.order.receiver=$scope.address.contact;//收货人
        orderService.submitOrder($scope.order).success(function (response) {
            if (response.flag){
                if ($scope.order.paymentType=='1'){
                    location.href="pay.html";
                }else {
                    location.href="ordersucces.html";
                }
            }else {
                alert(response.message);
            }
        })
    }
});