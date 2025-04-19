#!/bin/bash
set -e

# Wait for MinIO to be up
echo "Waiting for MinIO to start..."
until mc ping local >/dev/null 2>&1; do
  sleep 1
done

# Create default bucket if it doesn't exist
if ! mc ls local/docloader >/dev/null 2>&1; then
  echo "Creating default docloader bucket..."
  mc mb local/docloader
  echo "Setting bucket policy to public read..."
  # This is for development only - do not use this in production!
  mc policy set public local/docloader
  echo "Default bucket created and configured successfully."
else
  echo "Default bucket docloader already exists."
fi

# Create test tenant bucket if it doesn't exist
if ! mc ls local/test-tenant >/dev/null 2>&1; then
  echo "Creating test tenant bucket..."
  mc mb local/test-tenant
  echo "Setting bucket policy to public read..."
  # This is for development only - do not use this in production!
  mc policy set public local/test-tenant
  echo "Test tenant bucket created and configured successfully."
  
  # Add some test files to the test tenant bucket
  echo "Adding test files to test tenant bucket..."
  echo "This is a test document for the test tenant." > /tmp/test-document.txt
  mc cp /tmp/test-document.txt local/test-tenant/test-document.txt
  echo "Test files added to test tenant bucket."
else
  echo "Test tenant bucket already exists."
fi 