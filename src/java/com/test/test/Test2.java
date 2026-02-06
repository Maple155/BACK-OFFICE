package com.test.test;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import annotation.Controller;
import annotation.Get;
import annotation.GetURL;
import annotation.Param;
import service.ModelView;

@Controller
public class Test2 {

    @Get
    @GetURL(url = "/test2")
    public ModelView getTest2() {

        String[] hobbies = new String[2];
        hobbies[0] = "Manger";
        hobbies[1] = "Dormir";
        User user = new User("HARINAMBININA", "Ranto", 19, Date.valueOf(LocalDate.now()), hobbies, List.of("CEPE", "BEPC", "BACC"));

        ModelView model = new ModelView("test2.jsp");
        model.addObject("test", "Hello"); 
        model.addObject("test2", "Word");
        model.addObject("user", user);

        return model;
    }

    // URL: /test2/1
    @GetURL(url = "/test2/{id}")
    public int data1( /* String id */ ) {
        // return "ID reçu : " + id;

        return 12;
    }

    // URL: /test2/dates/22-02-06
    @GetURL(url = "/test2/dates/{date}")
    public Date data2( /* String date */ ) {
        // return "Date reçue : " + date;
        return Date.valueOf(LocalDate.now());
    }

    // URL: /users/123/posts/456
    @GetURL(url = "/users/{userId}/posts/{postId}")
    public String userPost( /* int userId, int postId */ ) {
        // return "User ID: " + userId + ", Post ID: " + postId;
        return "userPost";
    }

    @Get
    @GetURL(url = "/param/{id}")
    public String param1(@Param("id") Object param_id  ) {
        return "ID reçu du fonction param1 : " + param_id.toString();
    }

    @Get
    @GetURL(url = "/param/test/{id}")
    public String param2(Object id) {
        return "ID reçu du fonction param2 : " + id.toString();
    }

    @Get
    @GetURL(url = "/users/post/{userId}/posts/{postId}")
    public String userPost1(@Param("userId") int userId,@Param("postId") int postId ) {
        return "Fonction userPost1 -> User ID: " + userId + ", Post ID: " + postId;
    }

    @Get
    @GetURL(url = "/users/get/{userId}/posts/{postId}")
    public String userPost2( int userId, int postId ) {
        return "Fonction userPost2 -> User ID d: " + userId + ", Post ID: " + postId;
    }

} 