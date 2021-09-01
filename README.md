Dashbuilder
===========

Dashbuilder is a general purpose dashboard and reporting web app which allows for:

* Visual configuration and personalization of dashboards
* Support for different types of visualizations using several charting libraries
* Full featured editor for the definition of chart visualizations
* Definition of interactive report tables
* Data extraction from external systems, through different protocols
* Support for both analytics and real-time dashboards

Licensed under the Apache License, Version 2.0

For further information, please visit the project web site <a href="http://dashbuilder.org" target="_blank">dashbuilder.org</a>

Upcoming features
=================

* New renderers based on D3 JS, Lienzo GWT & Chart JS
* Hierarchical (nested group) displayer types: Tree & Pie
* Support for multiple dynamic data series
* Rich mobility support
* Alerts and SLA configuration
* RESTful API

Architecture
=================

* Not tied to any chart rendering technology. Pluggable renderers.
* No tied to any data storage.
* Ability to read data from: CSV files, Databases, Elastic Search orJava generators.
* Decoupled client & server layers. Ability to build pure lightweight client dashboards.
* Ability to push & handle data sets on client for better performance.
* Based on <a href="http://www.uberfireframework.org" target="_blank">Uberfire</a>, a framework for building rich workbench styled apps on the web.

