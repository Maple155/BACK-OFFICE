package com.test.test;

import java.sql.Date;
import java.time.LocalDate;
import annotation.Controller;
import annotation.Get;
import annotation.GetURL;
import annotation.Param;
import annotation.Post;
import annotation.RequestMapping;
import service.ModelView;

@Controller
public class Test1 {

    @Get
    @GetURL (url = "/test1/page")
    public ModelView test1page () 
    {
        ModelView model = new ModelView("test1.jsp");
        model.addObject("test", "Hello");
        model.addObject("test2", "Word");
    
        return model; 
    }    

    @Get
    @GetURL (url = "/test1")
    public String testHttpGet () 
    {
        return "Http Get";
    }

    @Post
    @GetURL (url = "/test1")
    public String testHttpPost () 
    {
        return "Http Post";
    }

    @RequestMapping
    @GetURL (url = "/test1/requestMapping")
    public String testRequestMapping () 
    {
        return "RequestMapping";
    }

    @Get
    @GetURL (url = "/test1/requestMapping")
    public String testRequestMapping1 () 
    {
        return "RequestMapping 1 ";
    }
}
