import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:frontend_ecotrack/core/services/api_client.dart';

class AuthService {
  final ApiClient _client;

  /// Constructor mới:
  /// - Nếu bạn truyền vào client: AuthService(client: myClient)
  /// - Nếu không truyền: AuthService()  -> tự tạo ApiClient + FlutterSecureStorage
  AuthService({ApiClient? client})
      : _client = client ?? ApiClient(storage: const FlutterSecureStorage());

  // Đăng nhập
  Future<bool> login(String email, String password) async {
    final res = await _client.post(
      '/api/auth/login',
      {
        'email': email,
        'password': password,
      },
    );

    if (res.statusCode == 200) {
      final data = _client.decodeUtf8Json(res);
      final token = data['token'];

      if (token != null) {
        await _client.storage.write(key: 'jwt_token', value: token);
        return true;
      }
    }

    print('Login failed: ${res.statusCode} - ${res.body}');
    return false;
  }

  // Đăng ký
  Future<bool> register(String email, String password) async {
    final res = await _client.post(
      '/api/auth/register',
      {
        'email': email,
        'username': email,
        'password': password,
      },
    );

    if (res.statusCode == 200) {
      final data = _client.decodeUtf8Json(res);
      final token = data['token'];

      if (token != null) {
        await _client.storage.write(key: 'jwt_token', value: token);
        print('Register successful, token saved.');
        return true;
      }
    }

    print('Register failed: ${res.statusCode} - ${res.body}');
    return false;
  }

  // Đăng xuất
  Future<void> logout() async {
    await _client.storage.delete(key: 'jwt_token');
  }

  // Lấy token hiện tại
  Future<String?> getToken() async {
    return await _client.storage.read(key: 'jwt_token');
  }
}
