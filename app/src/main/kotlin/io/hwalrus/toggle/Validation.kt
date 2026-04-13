package io.hwalrus.toggle

import org.http4k.lens.Path

internal val namePattern = Regex("^[a-zA-Z0-9_-]{1,100}$")
internal val groupName = Path.of("group")
