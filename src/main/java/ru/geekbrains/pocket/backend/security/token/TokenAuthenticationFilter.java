package ru.geekbrains.pocket.backend.security.token;

//https://habr.com/ru/post/278411/

public class TokenAuthenticationFilter { //extends AbstractAuthenticationProcessingFilter {
//
//    @Autowired
//    TokenAuthenticationManager authenticationManager;
//
//    public TokenAuthenticationFilter() {
//        super("/api/**");
//        setAuthenticationSuccessHandler((request, response, authentication) ->
//        {
//            SecurityContextHolder.getContext().setAuthentication(authentication);
//            request.getRequestDispatcher(request.getServletPath() + request.getPathInfo()).forward(request, response);
//        });
//        setAuthenticationFailureHandler((request, response, authenticationException) -> {
//            response.getOutputStream().print(authenticationException.getMessage());
//        });
//    }
//
//    @Override
//    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
//            throws AuthenticationException, IOException, ServletException {
//        String token = request.getHeader("token");
//        if (token == null)
//            token = request.getParameter("token");
//        if (token == null) {
//            TokenAuthentication authentication = new TokenAuthentication(null, null);
//            authentication.setAuthenticated(false);
//            return authentication;
//        }
//        TokenAuthentication tokenAuthentication = new TokenAuthentication(token);
//        Authentication authentication = getAuthenticationManager().authenticate(tokenAuthentication);
//        return authentication;
//    }
//
//    @Override
//    public void doFilter(ServletRequest req, ServletResponse res,
//                         FilterChain chain) throws IOException, ServletException {
//        super.doFilter(req, res, chain);
//    }
}
