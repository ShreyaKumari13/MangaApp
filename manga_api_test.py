import requests
import json

# API configuration exactly as shown in screenshots
url = "https://mangaverse-api.p.rapidapi.com/manga/fetch"

# Query parameters
params = {
    "page": "1",
    "genres": "Harem,Fantasy",
    "nsfw": "true",
    "type": "all"
}

# Headers
headers = {
    "X-RapidAPI-Key": "14a88debeamsh00fec5566b32637p1b1e0jsn52bd5dc0db14",
    "X-RapidAPI-Host": "mangaverse-api.p.rapidapi.com"
}

print("Making request to MangaVerse API...")
print(f"URL: {url}")
print(f"Params: {params}")
print(f"Headers: X-RapidAPI-Host: {headers['X-RapidAPI-Host']}")
print(f"Headers: X-RapidAPI-Key: {headers['X-RapidAPI-Key']}")

# Make the request
response = requests.get(url, headers=headers, params=params)

# Print response details
print(f"\nStatus Code: {response.status_code}")
print(f"Response Time: {response.elapsed.total_seconds():.2f} seconds")

# Process response
if response.status_code == 200:
    data = response.json()
    print("\nAPI Response Summary:")
    
    if 'data' in data:
        items = data.get('data', [])
        print(f"Total Manga Items: {len(items)}")
        
        if items and len(items) > 0:
            print("\nFirst 5 Manga Titles:")
            for i, item in enumerate(items[:5]):
                print(f"{i+1}. {item.get('title', 'Unknown Title')}")
                print(f"   - Genres: {', '.join(item.get('genres', []))}")
                print(f"   - NSFW: {item.get('nsfw', False)}")
                print(f"   - Type: {item.get('type', 'Unknown')}")
                print()
    
    # Save the full response to a file
    with open("manga_api_response.json", "w") as f:
        json.dump(data, f, indent=2)
        print("Full response saved to manga_api_response.json")
else:
    print(f"Error: {response.text}")
