# Pruebas unitarias locales (JVM)

Este directorio contiene un set basico de pruebas unitarias para logica de autenticacion y ViewModels.

## Cobertura inicial

- `data/local/TokenManagerTest`
- `data/repository/AuthRepositoryTest`
- `ui/screens/login/LoginViewModelTest`
- `ui/screens/register/RegisterViewModelTest`
- `ui/screens/home/HomeViewModelTest`

## Ejecutar

Desde la raiz del proyecto:

```powershell
.\gradlew.bat :app:testDebugUnitTest
```

