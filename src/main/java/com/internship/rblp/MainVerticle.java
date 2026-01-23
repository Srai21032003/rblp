package com.internship.rblp;

import com.internship.rblp.config.AppDatabaseConfig;
import com.internship.rblp.handlers.admin.*;
import com.internship.rblp.handlers.kyc.ApproveKycHandler;
import com.internship.rblp.handlers.kyc.GetAllKycHandler;
import com.internship.rblp.handlers.kyc.GetKycDetailHandler;
import com.internship.rblp.handlers.kyc.RejectKycHandler;
import com.internship.rblp.handlers.student.GetStudentKycStatusHandler;
import com.internship.rblp.handlers.student.SubmitStudentKycHandler;
import com.internship.rblp.handlers.student.SubmitTeacherKycHandler;
import com.internship.rblp.handlers.student.UpdateStudentProfileHandler;
import com.internship.rblp.handlers.teacher.GetTeacherKycStatusHandler;
import com.internship.rblp.handlers.teacher.UpdateTeacherProfileHandler;
import com.internship.rblp.repository.KycRepository;
import com.internship.rblp.repository.UserRepository;
import com.internship.rblp.routers.*;
import com.internship.rblp.handlers.auth.AuthHandler;
import com.internship.rblp.routers.AuthRouter;
import com.internship.rblp.routers.UserRouter;
import com.internship.rblp.service.*;
import io.reactivex.rxjava3.core.Completable;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.handler.AuthenticationHandler;
import io.vertx.rxjava3.config.ConfigRetriever;
import io.vertx.rxjava3.core.AbstractVerticle;
import io.vertx.rxjava3.core.http.HttpServer;
import io.vertx.rxjava3.ext.web.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(MainVerticle.class);

    @Override
    public Completable rxStart() {
        // to read application.properties
        ConfigStoreOptions fileStore = new ConfigStoreOptions()
                .setType("file")
                .setFormat("properties")
                .setConfig(new JsonObject().put("path", "application.properties"));

        ConfigRetrieverOptions options = new ConfigRetrieverOptions().addStore(fileStore);
        ConfigRetriever retriever = ConfigRetriever.create(vertx, options);

        // 2. Load Config -> Init DB -> Start Server
        return retriever.rxGetConfig()
                .flatMapCompletable(this::startApplication);
    }

    private Completable startApplication(JsonObject config) {
        try {
            AppDatabaseConfig.init(config);
        } catch (Exception e) {
            return Completable.error(e);
        }

        UserRepository userRepository = new UserRepository();
        KycRepository kycRepository = new KycRepository();

        AuthService authService = new AuthService(userRepository);
        AuthHandler.init(authService);

        AdminService adminService = new AdminService(userRepository);
        UpdateAdminProfileHandler.init(adminService);
        OnboardTeacherHandler.init(adminService);
        OnboardStudentHandler.init(adminService);
        ToggleUserStatusHandler.init(adminService);
        GetUserListHandler.init(adminService);

        KycService kycService = new KycService(kycRepository, userRepository);
        GetAllKycHandler.init(kycService);
        GetKycDetailHandler.init(kycService);
        ApproveKycHandler.init(kycService);
        RejectKycHandler.init(kycService);

        StudentService studentService = new StudentService(userRepository);
        UpdateStudentProfileHandler.init(studentService);
        SubmitStudentKycHandler.init(kycService);
        GetStudentKycStatusHandler.init(kycService);

        TeacherService teacherService = new TeacherService(userRepository);
        UpdateTeacherProfileHandler.init(teacherService);
        SubmitTeacherKycHandler.init(kycService);
        GetTeacherKycStatusHandler.init(kycService);

//        BulkUploadService bulkUploadService = new BulkUploadService(adminService);

        Router router = Router.router(vertx);

        router.get("/health").handler(ctx -> ctx.json(new JsonObject().put("status", "UP")));

        // Mount Sub-Routers
        //PUBLIC
        router.route("/api/auth/*").subRouter(AuthRouter.INSTANCE.create(vertx));

        //PROTECTED
        router.route("/api/user/*").subRouter(UserRouter.INSTANCE.create(vertx));
        router.route("/api/admin/*").subRouter(AdminRouter.INSTANCE.create(vertx));
        router.route("/api/student/*").subRouter(StudentRouter.INSTANCE.create(vertx));
        router.route("/api/teacher/*").subRouter(TeacherRouter.INSTANCE.create(vertx));


//        try {
//            System.out.println("DEBUG: Attempting to mount AuthRouter...");
////            router.mountSubRouter("/api/auth", AuthRouter.create(vertx));
//            router.route("/api/auth/*").subRouter(AuthRouter.create(vertx));
//            System.out.println("DEBUG: AuthRouter mounted successfully!");
//        } catch (Throwable t) {
//            System.err.println("CRITICAL ERROR: Failed to initialize Auth Module!");
//            t.printStackTrace(); // <--- This will print the REAL error to your console
//            return Completable.error(t);
//        }


        int port = Integer.parseInt(config.getString("server.port", "8080"));

        HttpServer server = vertx.createHttpServer();

        return server.requestHandler(router)
                .rxListen(port)
                .doOnSuccess(s -> logger.info("HTTP Server started on port {}", port))
                .doOnError(err -> logger.error("Failed to start HTTP server", err))
                .ignoreElement();
    }
}