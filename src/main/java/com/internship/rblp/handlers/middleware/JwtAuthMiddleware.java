package com.internship.rblp.handlers.middleware;

import com.internship.rblp.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.vertx.core.Handler;
import io.vertx.core.http.Cookie;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum JwtAuthMiddleware implements Handler<RoutingContext> {

    INSTANCE;
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthMiddleware.class);

    @Override
    public void handle(RoutingContext ctx){
        Cookie cookie = ctx.request().getCookie("authToken");
        String token = cookie.getValue();

        try{
            Claims claims = JwtUtil.validateToken(token);

            ctx.put("userId", claims.getSubject());
            ctx.put("role", claims.get("role"));
            ctx.put("email",claims.get("email"));

            ctx.next();
        } catch ( Exception e){
            logger.warn("Invalid token: {}",e.getMessage());

            ctx.response()
                    .setStatusCode(401)
                    .putHeader("Content-Type","application/json")
                    .end(new JsonObject().put("error","Invalid or expired token").encode());
        }
    }
}