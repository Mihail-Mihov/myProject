package com.example.myproject.web.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.SmartView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


public class UserInterceptor  implements HandlerInterceptor {

    private static Logger log = LoggerFactory.getLogger(UserInterceptor.class);

    public static boolean isUserLogged() {
        try {
            return !SecurityContextHolder.getContext().getAuthentication()
                    .getName().equals("anonymousUser");
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object object) throws Exception {
        if (isUserLogged()) {
            addToModelUserDetails(request.getSession());
        }
        return true;
    }

    private void addToModelUserDetails(HttpSession session) {
        log.info("=============== Добавяне на юзърнейма към UserDetails =========================");

        String loggedUsername
                = SecurityContextHolder.getContext().getAuthentication().getName();
        session.setAttribute("username", loggedUsername);

        log.info("Потребител: (" + loggedUsername + ") Сесия : " + session);
        log.info("=============== Добавяне на юзърнейма към UserDetails =========================");
    }

    @Override
    public void postHandle(
            HttpServletRequest req,
            HttpServletResponse res,
            Object o,
            ModelAndView model) throws Exception {

        if (model != null && !isRedirectView(model)) {
            if (isUserLogged()) {
                addToModelUserDetails(model);
            }
        }
    }

    public static boolean isRedirectView(ModelAndView mv) {
        String viewName = mv.getViewName();
        if (viewName.startsWith("redirect:/")) {
            return true;
        }
        View view = mv.getView();
        return (view != null && view instanceof SmartView
                && ((SmartView) view).isRedirectView());
    }

    private void addToModelUserDetails(ModelAndView model) {
        log.info("=============== Добавяне на юзърнейма към UserDetails =========================");

        String loggedUsername = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        model.addObject("loggedUsername", loggedUsername);

        log.trace("session : " + model.getModel());
        log.info("=============== Добавяне на юзърнейма към UserDetails =========================");
    }
}
