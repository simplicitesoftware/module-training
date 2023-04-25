Module Training configurations
---

---

<table>
<tr>
<td>UI</td>
<td>

```json
{
    "content_edition": {
        "mode": "UI"
    },
    "content_indexation": {
        "engine": "simplicite"
    }
}
```

</td>
<td>
Editing through UI, using Simplicité indexing.
</td>
</tr>
<tr>
<td>UI + ElasticSearch</td>
<td>

```json
{
    "content_edition": {
        "mode": "UI"
    },
    "content_indexation": {
        "engine": "elasticsearch",
        "esi_config": {
            "instance": "<instance url>",
            "index": "<elasticsearch index>",
            "public_credentials":"<user:password>"
        }
    }
}
```

</td>
<td>
The ElasticSearch mode works with every configuration, just change the value of the attribute<strong>engine</strong> to <strong>elasticsearch</strong>, and add the <strong>esi_config</strong> object as shown in the example.
</td>
</tr>
<tr>
<td>FILESYSTEM</td>
<td>

```json
{
    "content_edition": {
        "mode": "FILESYSTEM",
        "content_dir": "<path of the content directory>",
    },
    "content_indexation": {
        "engine": "simplicite"
    }
}
```

</td>
<td>
The content cannot be changed through the UI. Instead, the content is handled by the filesystem and is loaded in the module using the synchronisation mechanism.
</td>
</tr>
<tr>
<td>FILESYSTEM</br>{ Git { Simple }}</td>
<td>

```json
{
    "content_edition": {
        "mode": "FILESYSTEM",
        "content_dir": "<path of the content directory>",
        "git_checkout_service": {
            "repository": {
                "uri": "<uri>"
            }
        }
    },
    "content_indexation": {
        "engine": "simplicite"
    }
}
```

</td>
<td>
Same as above, but the content is fetched from a git repository.
</td>
</tr>
<tr>
<td>FILESYSTEM</br>{ Git { Authentified }}</td>
<td>

```json
{
    "content_edition": {
        "mode": "FILESYSTEM",
        "content_dir": "<path of the content directory>",
        "git_checkout_service": {
            "repository": {
                "uri": "<uri>",
                "creds": {
                    "username": "<username>",
                    "token": "<token>"
                } 
            }
        }
    },
    "content_indexation": {
        "engine": "simplicite"
    }
}
```

</td>
<td>
Same as above, with git credentials.
</td>
</tr>
</table>

<style>
    td {
        padding: 10px
    }
</style>