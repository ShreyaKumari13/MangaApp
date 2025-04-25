import requests
import json
import time

# API configuration
BASE_URL = "https://mangaverse-api.p.rapidapi.com"
HEADERS = {
    "X-RapidAPI-Key": "14a88debeamsh00fec5566b32637p1b1e0jsn52bd5dc0db14",
    "X-RapidAPI-Host": "mangaverse-api.p.rapidapi.com"
}

# Debug information
print("Using API Key:", HEADERS["X-RapidAPI-Key"])
print("Using Host:", HEADERS["X-RapidAPI-Host"])

def test_endpoint(endpoint, params=None, description=""):
    """Test a specific API endpoint and print results"""
    url = f"{BASE_URL}/{endpoint}"
    print(f"\n{'='*50}")
    print(f"Testing: {description}")
    print(f"URL: {url}")
    print(f"Params: {params}")
    print(f"{'='*50}")

    try:
        print(f"Making request to {endpoint}...")
        response = requests.get(url, headers=HEADERS, params=params, timeout=10)

        print(f"Status Code: {response.status_code}")
        print(f"Response Time: {response.elapsed.total_seconds():.2f} seconds")

        if response.status_code == 200:
            try:
                data = response.json()
                print("\nAPI Response Summary:")

                # Try to extract common fields based on endpoint
                if 'data' in data:
                    items = data.get('data', [])
                    print(f"Total Items: {len(items)}")

                    if items and len(items) > 0:
                        print("\nFirst 3 Items:")
                        for i, item in enumerate(items[:3]):
                            if 'title' in item:
                                print(f"{i+1}. {item.get('title', 'Unknown Title')}")
                            else:
                                print(f"{i+1}. {str(item)[:100]}...")

                # Save the full response to a file for inspection
                filename = f"api_response_{endpoint.replace('/', '_')}.json"
                with open(filename, "w") as f:
                    json.dump(data, f, indent=2)
                    print(f"\nFull response saved to {filename}")

                return True
            except json.JSONDecodeError:
                print(f"Error: Response is not valid JSON: {response.text[:200]}...")
        else:
            print(f"Error: {response.text}")

        return False
    except Exception as e:
        print(f"Exception occurred: {str(e)}")
        return False

# Test multiple endpoints
print("TESTING MANGAVERSE API")
print("=====================")

# Test 1: Fetch manga list - EXACT match from your Postman screenshot (no parameters)
test_endpoint(
    endpoint="manga/fetch",
    params=None,
    description="Fetch Manga List (No parameters - Exact Postman match)"
)

# Wait a bit between requests to avoid rate limiting
time.sleep(2)

# Test 2: Fetch manga list with parameters
test_endpoint(
    endpoint="manga/fetch",
    params={"page": "1", "genres": "Harem,Fantasy", "nsfw": "true", "type": "all"},
    description="Fetch Manga List (Page 1, Harem/Fantasy genres)"
)

# Wait a bit between requests to avoid rate limiting
time.sleep(2)

# Test 3: Simple fetch with just page parameter
test_endpoint(
    endpoint="manga/fetch",
    params={"page": "1"},
    description="Fetch Manga List (Page 1, no filters)"
)

time.sleep(2)

# Test 4: Search manga
test_endpoint(
    endpoint="manga/search",
    params={"query": "one piece"},
    description="Search for 'One Piece'"
)

time.sleep(2)

# Test 5: Fetch latest manga
test_endpoint(
    endpoint="manga/latest",
    params={},
    description="Fetch Latest Manga"
)

print("\nAPI Testing Complete!")
