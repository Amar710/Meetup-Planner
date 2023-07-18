package com.project.meetupplanner.Utilities;

import jakarta.servlet.http.HttpServletRequest;

public class EmailUtil {
    
    public static String getSiteUrl(HttpServletRequest request) {
        String siteURL = request.getRequestURL().toString();
        return siteURL.replace(request.getServletPath(), "");
    }
}
