import requests
import json

# Exactly match the Postman request
url = "https://mangaverse-api.p.rapidapi.com/manga/fetch"

# Headers exactly as shown in Postman
headers = {
    "X-RapidAPI-Key": "14a88debeamsh00fec5566b32637p1b1e0jsn52bd5dc0db14",
    "X-RapidAPI-Host": "mangaverse-api.p.rapidapi.com"
}

print("Making request to MangaVerse API...")
print(f"URL: {url}")
print(f"Headers: {headers}")

# Make the request without any parameters (just like in your screenshot)
response = requests.get(url, headers=headers)

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
            print("\nFirst 3 Manga Titles:")
            for i, item in enumerate(items[:3]):
                print(f"{i+1}. {item.get('title', 'Unknown Title')}")
                print(f"   - Genres: {', '.join(item.get('genres', []))}")
                print(f"   - NSFW: {item.get('nsfw', False)}")
                print(f"   - Type: {item.get('type', 'Unknown')}")
                print()
    
    # Save the full response to a file
    with open("postman_exact_response.json", "w") as f:
        json.dump(data, f, indent=2)
        print("Full response saved to postman_exact_response.json")
else:
    print(f"Error: {response.text}")
