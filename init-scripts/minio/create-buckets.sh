#!/bin/sh

# Wait for MinIO to be ready
echo "Waiting for MinIO to be ready..."
until mc ready local; do
  sleep 1
done

echo "Creating default bucket..."
mc mb local/docloader --ignore-existing

echo "Setting policy on default bucket..."
mc policy set download local/docloader

echo "MinIO initialization completed" 