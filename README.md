<!--
 ___ _            _ _    _ _    __
/ __(_)_ __  _ __| (_)__(_) |_ /_/
\__ \ | '  \| '_ \ | / _| |  _/ -_)
|___/_|_|_|_| .__/_|_\__|_|\__\___|
            |_| 
-->
![](https://docs.simplicite.io//logos/logo250.png)
* * *

`Training` module definition
============================

The module for the Simplciité Training platform

`TrnCategory` business object definition
----------------------------------------

The object that serves as container for lessons, or other categories (reflexivity)

### Fields

| Name                                                         | Type                                     | Required | Updatable | Personal | Description                                                                      | 
| ------------------------------------------------------------ | ---------------------------------------- | -------- | --------- | -------- | -------------------------------------------------------------------------------- |
| `trnCatPath`                                                 | char(400)                                | *        |           |          | -                                                                                |
| `trnCatOrder`                                                | int(100)                                 | yes      | yes       |          | -                                                                                |
| `trnCatTitle`                                                | char(200)                                | yes      | yes       |          | -                                                                                |
| `trnCatDescription`                                          | text(1000)                               |          | yes       |          | -                                                                                |
| `trnCatPublish`                                              | boolean                                  |          | yes       |          | Determines if the category and its subcategories and lessons are available on the front-end application. |
| `trnCatId` link to **`TrnCategory`**                         | id                                       |          | yes       |          | -                                                                                |
| _Ref. `trnCatId.trnCatPath`_                                 | _char(400)_                              |          |           |          | -                                                                                |
| _Ref. `trnCatId.trnCatTitle`_                                | _char(200)_                              |          |           |          | -                                                                                |

`TrnCategoryTranslate` business object definition
-------------------------------------------------

This object contains the translation of a category object

### Fields

| Name                                                         | Type                                     | Required | Updatable | Personal | Description                                                                      | 
| ------------------------------------------------------------ | ---------------------------------------- | -------- | --------- | -------- | -------------------------------------------------------------------------------- |
| `trnCtrLang`                                                 | enum(100) using `LANG_ALL` list          | yes*     | yes       |          | -                                                                                |
| `trnCtrTitle`                                                | char(200)                                | yes*     | yes       |          | -                                                                                |
| `trnCtrDescription`                                          | text(1000)                               |          | yes       |          | -                                                                                |
| `trnCtrPicture`                                              | image                                    |          | yes       |          | The picture associated with a category.                                          |
| `trnCtrCatId` link to **`TrnCategory`**                      | id                                       |          | yes       |          | -                                                                                |
| _Ref. `trnCtrCatId.trnCatTitle`_                             | _char(200)_                              |          |           |          | -                                                                                |

### Lists

* `LANG_ALL`
    - `ANY` All languages
    - `ENU` English language
    - `FRA` French language

`TrnLesson` business object definition
--------------------------------------

The lesson object for the training, that will hold the content of a lesson.

### Fields

| Name                                                         | Type                                     | Required | Updatable | Personal | Description                                                                      | 
| ------------------------------------------------------------ | ---------------------------------------- | -------- | --------- | -------- | -------------------------------------------------------------------------------- |
| `trnLsnPath`                                                 | char(400)                                | *        |           |          | -                                                                                |
| `trnLsnOrder`                                                | int(100)                                 | yes      | yes       |          | -                                                                                |
| `trnLsnTitle`                                                | char(200)                                | yes      | yes       |          | -                                                                                |
| `trnLsnDescription`                                          | text(1000)                               |          | yes       |          | -                                                                                |
| `trnLsnVideo`                                                | document                                 |          | yes       |          | -                                                                                |
| `trnLsnContent`                                              | text(200000)                             |          | yes       |          | -                                                                                |
| `trnLsnHtmlContent`                                          | text(20000)                              |          |           |          | -                                                                                |
| `trnLsnVisualization`                                        | enum(6) using `TRNLSNVISUALIZATION` list |          | yes       |          | Describes the visualization mode to be used in the front-end application.        |
| `trnLsnPublish`                                              | boolean                                  |          | yes       |          | Determines if the lesson is visible on the front-end application.                |
| `trnLsnCatId` link to **`TrnCategory`**                      | id                                       | yes      | yes       |          | -                                                                                |
| _Ref. `trnLsnCatId.trnCatTitle`_                             | _char(200)_                              |          |           |          | -                                                                                |
| _Ref. `trnLsnCatId.trnCatPath`_                              | _char(400)_                              |          |           |          | -                                                                                |
| _Ref. `trnLsnCatId.trnCatId`_                                | _id_                                     |          |           |          | -                                                                                |
| _Ref. `trnCatId.trnCatTitle`_                                | _char(200)_                              |          |           |          | -                                                                                |

### Lists

* `TRNLSNVISUALIZATION`
    - `TUTO` Tutorial
    - `LINEAR` Linear

`TrnLsnTranslate` business object definition
--------------------------------------------

The object used to translate the lesson objects, for multilingual transport.

### Fields

| Name                                                         | Type                                     | Required | Updatable | Personal | Description                                                                      | 
| ------------------------------------------------------------ | ---------------------------------------- | -------- | --------- | -------- | -------------------------------------------------------------------------------- |
| `trnLtrLan`                                                  | enum(100) using `LANG_ALL` list          | yes*     | yes       |          | -                                                                                |
| `trnLtrTitle`                                                | char(200)                                | yes*     | yes       |          | -                                                                                |
| `trnLtrDescription`                                          | text(1000)                               |          | yes       |          | -                                                                                |
| `trnLtrContent`                                              | text(20000)                              |          | yes       |          | -                                                                                |
| `trnLtrVideo`                                                | document                                 |          | yes       |          | -                                                                                |
| `trnLtrHtmlContent`                                          | html(50000)                              |          | yes       |          | The HTML equivalent of the content attribute. It's this attribute that is displayed by the front-end application. |
| `trnLtrLsnId` link to **`TrnLesson`**                        | id                                       |          | yes       |          | -                                                                                |
| _Ref. `trnLtrLsnId.trnLsnPath`_                              | _char(400)_                              |          |           |          | -                                                                                |

### Lists

* `LANG_ALL`
    - `ANY` All languages
    - `ENU` English language
    - `FRA` French language

`TrnPicture` business object definition
---------------------------------------

The picture object for the lessons. Used for the multilanguage support.

### Fields

| Name                                                         | Type                                     | Required | Updatable | Personal | Description                                                                      | 
| ------------------------------------------------------------ | ---------------------------------------- | -------- | --------- | -------- | -------------------------------------------------------------------------------- |
| `trnPicImage`                                                | image                                    | yes*     | yes       |          | -                                                                                |
| `trnPicLang`                                                 | enum(100) using `LANG_ALL` list          | yes*     | yes       |          | -                                                                                |
| `trnPicLsnId` link to **`TrnLesson`**                        | id                                       |          | yes       |          | -                                                                                |
| _Ref. `trnPicLsnId.trnLsnPath`_                              | _char(400)_                              |          |           |          | -                                                                                |

### Lists

* `LANG_ALL`
    - `ANY` All languages
    - `ENU` English language
    - `FRA` French language

`TrnSiteContent` business object definition
-------------------------------------------

This object will hold the static contents of the front-end application

### Fields

| Name                                                         | Type                                     | Required | Updatable | Personal | Description                                                                      | 
| ------------------------------------------------------------ | ---------------------------------------- | -------- | --------- | -------- | -------------------------------------------------------------------------------- |
| `trnSitCode`                                                 | char(50)                                 | yes*     | yes       |          | A code identifying the TrnSiteContent object                                     |
| `trnSitLanguage`                                             | enum(5) using `LANG_ALL` list            | yes*     | yes       |          | -                                                                                |
| `trnSitContent`                                              | text(100000)                             |          | yes       |          | -                                                                                |

### Lists

* `LANG_ALL`
    - `ANY` All languages
    - `ENU` English language
    - `FRA` French language

`TrnExternalTreeView` external object definition
------------------------------------------------

External Object that sends back a treeView of the caategory and lessons hierarchy


`TrnFront` external object definition
-------------------------------------




`TrnGitEndPoint` external object definition
-------------------------------------------

This endpoint will be hit by the Git repository that contains the training content when a new commit is made on the master branch. This will cause the server to pull those modifications and add them to the database.


`TrnSyncService` external object definition
-------------------------------------------




`TrnTreeService` external object definition
-------------------------------------------




