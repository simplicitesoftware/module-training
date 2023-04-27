<!--
 ___ _            _ _    _ _    __
/ __(_)_ __  _ __| (_)__(_) |_ /_/
\__ \ | '  \| '_ \ | / _| |  _/ -_)
|___/_|_|_|_| .__/_|_\__|_|\__\___|
            |_| 
-->
![](https://docs.simplicite.io//logos/logo250.png)
* * *

Sonar Analysis
====================

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=simplicitesoftware_module-training&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=simplicitesoftware_module-training)

Installation instructions
====================

1. deploy a Simplicité instance with internet access
2. navigate to AppStore > Apps > Documentation and click on the Install button
3. clear the cache

**WARNING:** At the moment, the modules takes over the root URL (`/`), deploying a specific instance for this module is highly recommended to avoid interfering with other modules.

`Training` module definition
============================

The module for the Simplicité Training platform.

This module objective is to offer an easy way to deploy a custom documentation.
It's a *Category* / *Lesson* / *Translations* model with a few additions such as *tags* or *pages*.

The training module not only gives the ability to manage content, but it also serves a Vue.js front end (see the external object *TrnFront*).

The front end is served on the root url of your instance. For further details on the site mapping, see system parameter *URI_MAPPING*.

More info available on the front end default home page.