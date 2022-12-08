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

`TrnSiteContent` business object definition
-------------------------------------------

This object will hold the static contents of the front-end application

### Fields

| Name                                                         | Type                                     | Required | Updatable | Personal | Description                                                                      |
|--------------------------------------------------------------|------------------------------------------|----------|-----------|----------|----------------------------------------------------------------------------------|
| `trnSitCode`                                                 | char(50)                                 | yes*     | yes       |          | A code identifying the TrnSiteContent object                                     |
| `trnSitLanguage`                                             | enum(5) using `LANG_ALL` list            | yes*     | yes       |          | -                                                                                |
| `trnSitContent`                                              | text(100000)                             |          | yes       |          | -                                                                                |

### Lists

* `LANG_ALL`
    - `ANY` All languages
    - `ENU` English language
    - `FRA` French language

`TrnCategory` business object definition
----------------------------------------

The object that serves as container for lessons, or other categories (reflexivity)

### Fields

| Name                                                         | Type                                     | Required | Updatable | Personal | Description                                                                      |
|--------------------------------------------------------------|------------------------------------------|----------|-----------|----------|----------------------------------------------------------------------------------|
| `trnCatPublish`                                              | boolean                                  |          | yes       |          | Determines if the category and its subcategories and lessons are available on the front-end application. |
| `trnCatPath`                                                 | text(400)                                | *        | yes       |          | -                                                                                |
| `trnCatOrder`                                                | int(100)                                 | yes      | yes       |          | -                                                                                |
| `trnCatCode`                                                 | char(100)                                | yes      | yes       |          | -                                                                                |
| `trnCatFrontPath`                                            | url(400)                                 |          |           |          | -                                                                                |
| `trnCatId` link to **`TrnCategory`**                         | id                                       |          | yes       |          | -                                                                                |
| _Ref. `trnCatId.trnCatPath`_                                 | _text(400)_                              |          |           |          | -                                                                                |

### Custom actions

* `forceDirSync`: 

`TrnCategoryTranslate` business object definition
-------------------------------------------------

This object contains the translation of a category object

### Fields

| Name                                                         | Type                                     | Required | Updatable | Personal | Description                                                                      |
|--------------------------------------------------------------|------------------------------------------|----------|-----------|----------|----------------------------------------------------------------------------------|
| `trnCtrLang`                                                 | enum(100) using `LANG_ALL` list          | yes*     | yes       |          | -                                                                                |
| `trnCtrTitle`                                                | char(200)                                | yes      | yes       |          | -                                                                                |
| `trnCtrDescription`                                          | text(1000)                               |          | yes       |          | -                                                                                |
| `trnCtrCatId` link to **`TrnCategory`**                      | id                                       | *        | yes       |          | -                                                                                |
| _Ref. `trnCtrCatId.trnCatPath`_                              | _text(400)_                              |          |           |          | -                                                                                |

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
|--------------------------------------------------------------|------------------------------------------|----------|-----------|----------|----------------------------------------------------------------------------------|
| `trnLsnPublish`                                              | boolean                                  |          | yes       |          | Determines if the lesson is visible on the front-end application.                |
| `trnLsnPath`                                                 | text(400)                                | *        |           |          | -                                                                                |
| `trnLsnOrder`                                                | int(100)                                 | yes      | yes       |          | -                                                                                |
| `trnLsnCode`                                                 | char(255)                                | yes      | yes       |          | -                                                                                |
| `trnLsnVisualization`                                        | enum(6) using `TRNLSNVISUALIZATION` list |          | yes       |          | Describes the visualization mode to be used in the front-end application.        |
| `trnLsnFrontPath`                                            | url(400)                                 |          |           |          | -                                                                                |
| `trnLsnCatId` link to **`TrnCategory`**                      | id                                       | yes      | yes       |          | -                                                                                |
| _Ref. `trnLsnCatId.trnCatPath`_                              | _text(400)_                              |          |           |          | -                                                                                |
| `trnTagLsnVirtual` link to **`TrnTag`**                      | id                                       |          | yes       |          | -                                                                                |

### Lists

* `TRNLSNVISUALIZATION`
    - `TUTO` Tutorial
    - `LINEAR` Linear

`TrnLsnTranslate` business object definition
--------------------------------------------

The object used to translate the lesson objects, for multilingual transport.

### Fields

| Name                                                         | Type                                     | Required | Updatable | Personal | Description                                                                      |
|--------------------------------------------------------------|------------------------------------------|----------|-----------|----------|----------------------------------------------------------------------------------|
| `trnLtrLang`                                                 | enum(100) using `LANG_ALL` list          | yes*     | yes       |          | -                                                                                |
| `trnLtrTitle`                                                | char(200)                                | yes      | yes       |          | -                                                                                |
| `trnLtrDescription`                                          | text(1000)                               |          | yes       |          | -                                                                                |
| `trnLtrContent`                                              | text(100000)                             |          | yes       |          | -                                                                                |
| `trnLtrVideo`                                                | document                                 |          | yes       |          | -                                                                                |
| `trnLtrHtmlContent`                                          | text(500000)                             |          |           |          | The HTML equivalent of the content attribute. It's this attribute that is displayed by the front-end application. |
| `trnLtrLsnId` link to **`TrnLesson`**                        | id                                       | *        | yes       |          | -                                                                                |
| _Ref. `trnLtrLsnId.trnLsnPath`_                              | _text(400)_                              |          |           |          | -                                                                                |
| `trnLtrRawContent`                                           | text(10000)                              |          |           |          | -                                                                                |

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
|--------------------------------------------------------------|------------------------------------------|----------|-----------|----------|----------------------------------------------------------------------------------|
| `trnPicId`                                                   | id                                       | *        | yes       |          | -                                                                                |
| `trnPicLang`                                                 | enum(100) using `LANG_ALL` list          | yes      | yes       |          | -                                                                                |
| `trnPicImage`                                                | image                                    | yes      | yes       |          | -                                                                                |
| `trnPicLsnId` link to **`TrnLesson`**                        | id                                       | yes      | yes       |          | -                                                                                |
| _Ref. `trnPicLsnId.trnLsnPath`_                              | _text(400)_                              |          |           |          | -                                                                                |

### Lists

* `LANG_ALL`
    - `ANY` All languages
    - `ENU` English language
    - `FRA` French language

`TrnTag` business object definition
-----------------------------------

Object that contains lessons tags.

### Fields

| Name                                                         | Type                                     | Required | Updatable | Personal | Description                                                                      |
|--------------------------------------------------------------|------------------------------------------|----------|-----------|----------|----------------------------------------------------------------------------------|
| `trnTagCode`                                                 | char(255)                                | yes*     | yes       |          | -                                                                                |

`TrnTagTranslate` business object definition
--------------------------------------------

Translation of tag object.

### Fields

| Name                                                         | Type                                     | Required | Updatable | Personal | Description                                                                      |
|--------------------------------------------------------------|------------------------------------------|----------|-----------|----------|----------------------------------------------------------------------------------|
| `trnTagTranslateLang`                                        | enum(255) using `LANG_ALL` list          | yes*     | yes       |          | -                                                                                |
| `trnTagTranslateTrad`                                        | char(255)                                | yes*     | yes       |          | Traduction content                                                               |
| `trnTaglangTagId` link to **`TrnTag`**                       | id                                       |          | yes       |          | -                                                                                |
| _Ref. `trnTaglangTagId.trnTagCode`_                          | _char(255)_                              |          |           |          | -                                                                                |

### Lists

* `LANG_ALL`
    - `ANY` All languages
    - `ENU` English language
    - `FRA` French language

`TrnTagLsn` business object definition
--------------------------------------



### Fields

| Name                                                         | Type                                     | Required | Updatable | Personal | Description                                                                      |
|--------------------------------------------------------------|------------------------------------------|----------|-----------|----------|----------------------------------------------------------------------------------|
| `trnTaglsnTagId` link to **`TrnTag`**                        | id                                       | yes*     | yes       |          | -                                                                                |
| _Ref. `trnTaglsnTagId.trnTagCode`_                           | _char(255)_                              |          |           |          | -                                                                                |
| `trnTaglsnLsnId` link to **`TrnLesson`**                     | id                                       | yes*     | yes       |          | -                                                                                |
| _Ref. `trnTaglsnLsnId.trnLsnPath`_                           | _text(400)_                              |          |           |          | -                                                                                |

`TrnFront` external object definition
-------------------------------------




`TrnPublicService` external object definition
---------------------------------------------




`TrnSyncService` external object definition
-------------------------------------------




`TrnTagService` external object definition
------------------------------------------

sends a JSON containing tags and theirs traduction in the current front langage


`TrnTreeService` external object definition
-------------------------------------------




