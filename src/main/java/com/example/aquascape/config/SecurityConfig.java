import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.core.Ordered;
import org.springframework.web.filter.CorsFilter;

@Bean
public FilterRegistrationBean<CorsFilter> customCorsFilter() {
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    CorsConfiguration config = new CorsConfiguration();
    
    config.setAllowCredentials(true);
    config.setAllowedOrigins(Arrays.asList(
        "http://localhost:5001", 
        "http://127.0.0.1:5001", 
        "http://localhost:3000",
        "http://localhost:5173", 
        "https://your-aquascape-web.com"
    ));
    config.setAllowedHeaders(Arrays.asList("*"));
    config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    config.setMaxAge(3600L);
    
    source.registerCorsConfiguration("/**", config);
    
    FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(source));
    bean.setOrder(Ordered.HIGHEST_PRECEDENCE); 
    return bean;
}