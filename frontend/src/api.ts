/// <reference types="vite/client" />

export const API_ORIGIN: string = import.meta.env.PROD
  ? ""
  : (import.meta.env.VITE_API_URL ?? "http://localhost:8080");
const API_BASE_URL = `${API_ORIGIN}/api`;

export async function apiFetch(
  endpoint: string,
  options: RequestInit = {}
) {
  const token = localStorage.getItem("keystone_token");

  const headers = new Headers(options.headers);

  headers.set("Content-Type", "application/json");

  if (token) {
    headers.set("Authorization", `Bearer ${token}`);
  }

  const url = endpoint.startsWith("/api/")
    ? `${API_ORIGIN}${endpoint}`
    : `${API_BASE_URL}${endpoint}`;

  const response = await fetch(url, {
    ...options,
    headers,
  });

  const text = await response.text();

  let data: any;

  try {
    data = text ? JSON.parse(text) : null;
  } catch {
    data = text;
  }

  if (!response.ok) {
    throw new Error(
      data?.message ||
        data?.error ||
        `Request failed: ${response.status}`
    );
  }

  return data;
}