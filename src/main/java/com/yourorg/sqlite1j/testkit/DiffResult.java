package com.yourorg.sqlite1j.testkit;

import java.util.List;

public record DiffResult(boolean matches, List<String> differences) {
}
