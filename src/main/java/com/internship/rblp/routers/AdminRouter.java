package com.internship.rblp.routers;

import com.internship.rblp.handlers.admin.bulkupload.GetBulkErrorsHandler;
import com.internship.rblp.handlers.admin.bulkupload.GetBulkStatusHandler;
import com.internship.rblp.handlers.admin.bulkupload.StartBulkUploadHandler;
import com.internship.rblp.handlers.middleware.JwtAuthMiddleware;
import com.internship.rblp.handlers.admin.UpdateAdminProfileHandler;
import com.internship.rblp.handlers.admin.OnboardStudentHandler;
import com.internship.rblp.handlers.admin.OnboardTeacherHandler;
import com.internship.rblp.handlers.admin.GetUserListHandler;
import com.internship.rblp.handlers.admin.ToggleUserStatusHandler;
import com.internship.rblp.handlers.middleware.RoleMiddleware;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.Router;
import io.vertx.rxjava3.ext.web.handler.BodyHandler;

public enum AdminRouter {

    INSTANCE;

    public Router create(Vertx vertx) {
        Router router = Router.router(vertx);

        router.route().handler(BodyHandler.create());
        router.route().handler(JwtAuthMiddleware.INSTANCE);
        router.route().handler(RoleMiddleware.ADMIN);

        // routes
        router.put("/profile").handler(UpdateAdminProfileHandler.INSTANCE);
        router.post("/onboard/student").handler(OnboardStudentHandler.INSTANCE);
        router.post("/onboard/teacher").handler(OnboardTeacherHandler.INSTANCE);

        router.get("/users").handler(GetUserListHandler.INSTANCE);
        router.put("/users/:id/toggle").handler(ToggleUserStatusHandler.INSTANCE);

        router.post("/upload/bulk").handler(StartBulkUploadHandler.INSTANCE);
        router.get("/upload/:id/status").handler(GetBulkStatusHandler.INSTANCE);
        router.get("/upload/:id/errors").handler(GetBulkErrorsHandler.INSTANCE);

        router.route("/kyc/*").subRouter(KycRouter.INSTANCE.create(vertx));

        return router;
    }
}