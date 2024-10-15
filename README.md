# multitargetproxyservlet
Send an http - request to multiple target uris.

# Description
This project presents a multi-target servlet.
The servlet send one request to multiple target uris.

_Note_: Only the response of the **first** target uri is send back.

This project is inspired by https://github.com/mitre/HTTP-Proxy-Servlet.

Project coordinate:
```xml
<groupId>org.huberb</groupId>
<artifactId>proxyservlet</artifactId>
<version>1.0-SNAPSHOT</version>
<packaging>war</packaging>
```

# Usage

- Define a servlet `multi-proxy-servlet` with servlet-class `org.huberb.proxyservlet.MultiProxyingServlet`
- Map the serlvet to path `/multi-proxy-servlet/index.html`
- Define one or more targetUris like `targetUri.1 -> /multiproxyingservlet/a.html`

The definitions above proxy from `/multi-proxy-servlet/index.html` to all defined 
target uris.

* You can specify a single targetUri as "targetUri".
* You can specify multiple targetUris as "targetUris=targetUri1,targetUri2..."
* You can specify mutliple targetUris as "targetUri.1=targetUri1", "targetUri.2=targetUri2", ...

# Todos
- Add more unit tests

# Web xml example

More complete web.xml defining multi target proxy uri.

```
<servlet>
    <description>use servlet-init-parameter target-uris</description>
    <servlet-name>multi-proxy-servlet</servlet-name>
    <servlet-class>org.huberb.proxyservlet.MultiProxyingServlet</servlet-class>
    <init-param>
        <param-name>targetUri.1</param-name>
        <param-value>/multiproxyingservlet/a.html</param-value>
    </init-param>
    <init-param>
        <param-name>targetUri.2</param-name>
        <param-value>/multiproxyingservlet/b.html</param-value>
    </init-param>
    <init-param>
        <param-name>targetUri.3</param-name>
        <param-value>/multiproxyingservlet/c.html</param-value>
    </init-param>
    <init-param>
        <param-name>targetUri.4</param-name>
        <param-value>/multiproxyingservlet/d.html</param-value>
    </init-param>
    <init-param>
        <param-name>targetUri.5</param-name>
        <param-value>/multiproxyingservlet/e.html</param-value>
    </init-param>
    <init-param>
        <param-name>targetUri.6</param-name>
        <param-value>/multiproxyingservlet/f.html</param-value>
    </init-param>
</servlet>
```


