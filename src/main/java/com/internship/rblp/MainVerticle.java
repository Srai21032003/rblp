package com.internship.rblp;

import com.internship.rblp.config.AppDatabaseConfig;
import com.internship.rblp.repository.KycRepository;
import com.internship.rblp.repository.UserRepository;
import com.internship.rblp.routers.AuthRouter;
import com.internship.rblp.routers.UserRouter;
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
        // 1. Setup Config Retriever to read 'application.properties'
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
        // A. Initialize Database (Sync operation)
        // We wrap this in a try-catch block because it's blocking/synchronous
        try {
            AppDatabaseConfig.init(config);
        } catch (Exception e) {
            return Completable.error(e);
        }

        UserRepository userRepository = new UserRepository();
        KycRepository kycRepository = new KycRepository();

        AuthService authService = new AuthService(userRepository);
        AuthHandler.init(authService);

        AdminService adminService = new AdminService(userRepository); // Update AdminService too
//        StudentService studentService = new StudentService(userRepository); // Update StudentService
//        TeacherService teacherService = new TeacherService(userRepository); // Update TeacherService
        KycService kycService = new KycService(kycRepository, userRepository);
//        BulkUploadService bulkUploadService = new BulkUploadService(adminService);

        Router router = Router.router(vertx);

        // Simple health check route to verify server is up
        router.get("/health").handler(ctx -> ctx.json(new JsonObject().put("status", "UP")));

        // Mount Sub-Routers
        //PUBLIC
        router.route("/api/auth/*").subRouter(AuthRouter.INSTANCE.create(vertx));

        //PROTECTED
        router.route("/api/user/*").subRouter(UserRouter.INSTANCE.create(vertx));


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
        // C. Start HTTP Server
        int port = Integer.parseInt(config.getString("server.port", "8080"));

        HttpServer server = vertx.createHttpServer();

        return server.requestHandler(router)
                .rxListen(port)
                .doOnSuccess(s -> logger.info("HTTP Server started on port {}", port))
                .doOnError(err -> logger.error("Failed to start HTTP server", err))
                .ignoreElement(); // Convert Single<HttpServer> to Completable
    }
}