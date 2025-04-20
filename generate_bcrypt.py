#!/usr/bin/env python3
import bcrypt
import sys

def generate_bcrypt_hash(password):
    # Generate salt and hash the password
    password_bytes = password.encode('utf-8')
    salt = bcrypt.gensalt(rounds=10)  # 10 is a good default
    hashed = bcrypt.hashpw(password_bytes, salt)
    return hashed.decode('utf-8')

if __name__ == "__main__":
    if len(sys.argv) > 1:
        password = sys.argv[1]
    else:
        password = input("Enter password to hash: ")
    
    hashed_password = generate_bcrypt_hash(password)
    print("\nBCrypt hash for Spring Security:")
    print(hashed_password)
    print("\nSQL Update command:")
    print(f"UPDATE app.users SET password_hash = '{hashed_password}' WHERE username IN ('admin', 'sysadmin');") 