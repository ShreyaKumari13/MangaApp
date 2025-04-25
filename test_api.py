import requests
import json

# API configuration exactly as shown in Postman
url = "https://mangaverse-api.p.rapidapi.com/manga/fetch"

# Headers - exactly as shown in your Postman screenshot
headers = {
    "X-RapidAPI-Key": "14a88debeamsh00fec5566b32637p11d1e0jsn52bd5dc0db14",
    "X-RapidAPI-Host": "mangaverse-api.p.rapidapi.com"
}

print("Making request to MangaVerse API...")
print(f"URL: {url}")
print(f"Headers: {headers}")

try:
    # Make the request with no query parameters (just like in your screenshot)
    response = requests.get(url, headers=headers)
    
    # Print response details
    print(f"\nStatus Code: {response.status_code}")
    print(f"Response Time: {response.elapsed.total_seconds():.2f} seconds")
    
    if response.status_code == 200:
        data = response.json()
        print("\nAPI Response Summary:")
        
        # Check the structure of the response
        print(f"Response Keys: {list(data.keys())}")
        
        if 'data' in data:
            items = data.get('data', [])
            print(f"Total Manga Items: {len(items)}")
            
            if items and len(items) > 0:
                print("\nFirst Manga Item:")
                first_item = items[0]
                print(f"Keys in first item: {list(first_item.keys())}")
                print(f"Title: {first_item.get('title', 'Unknown')}")
                print(f"ID: {first_item.get('id', 'Unknown')}")
                
        # Save the full response to a file for inspection
        with open("api_response.json", "w") as f:
            json.dump(data, f, indent=2)
            print("\nFull response saved to api_response.json")
    else:
        print(f"Error: {response.text}")
        
except Exception as e:
    print(f"Exception occurred: {str(e)}")
