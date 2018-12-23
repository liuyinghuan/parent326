package cn.itcast.core.service;

import cn.itcast.core.pojo.seller.Seller;
import cn.itcast.core.service.seller.SellerService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.HashSet;
import java.util.Set;

/**
 * 自定义的认证类
 */
public class UserDetailServiceImpl implements UserDetailsService{

    // 注入SellerService
    private SellerService sellerService;
    public void setSellerService(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    /**
     * 判断该用户是否存在
     * @param username
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Seller seller = sellerService.findOne(username);
        if(seller != null && "1".equals(seller.getStatus())){ // 必须是审核通过后的商家
            Set<GrantedAuthority> authorities = new HashSet<>();
            SimpleGrantedAuthority simpleGrantedAuthority = new SimpleGrantedAuthority("ROLE_SELLER");
            authorities.add(simpleGrantedAuthority);
            // 用户名称、密码、该用户的访问权限
            User user = new User(username, seller.getPassword(), authorities);
            return user;
        }

        return null;
    }
}
