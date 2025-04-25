import requests

url = "https://mangaverse-api.p.rapidapi.com/manga/fetch"

headers = {
    "X-RapidAPI-Key": "14a88debeamsh00fec5566b32637p1b1e0jsn52bd5dc0db14",
    "X-RapidAPI-Host": "mangaverse-api.p.rapidapi.com"
}

# Print request details
print(f"Making request to: {url}")
print(f"Headers: {headers}")

# Make the request with no parameters
response = requests.request("GET", url, headers=headers)

# Print response details
print(f"\nStatus Code: {response.status_code}")
print(f"Response Headers: {dict(response.headers)}")
print(f"Response Content: {response.text[:500]}...")  # First 500 chars
