import requests
import json

url = "https://mangaverse-api.p.rapidapi.com/manga/fetch"

# Exactly match the headers from Postman
headers = {
    "X-RapidAPI-Key": "14a88debeamsh00fec5566b32637p1b1e0jsn52bd5dc0db14",
    "X-RapidAPI-Host": "mangaverse-api.p.rapidapi.com"
}

print("Making request to MangaVerse API...")
print(f"URL: {url}")
print(f"Headers: {headers}")

try:
    # Make the request with no query parameters
    response = requests.request("GET", url, headers=headers)
    
    # Print response details
    print(f"\nStatus Code: {response.status_code}")
    print(f"Response Time: {response.elapsed.total_seconds():.2f} seconds")
    
    if response.status_code == 200:
        try:
            data = response.json()
            print("\nAPI Response Summary:")
            
            if 'data' in data:
                items = data.get('data', [])
                print(f"Total Manga Items: {len(items)}")
                
                if items and len(items) > 0:
                    print("\nFirst 3 Manga Titles:")
                    for i, item in enumerate(items[:3]):
                        print(f"{i+1}. {item.get('title', 'Unknown Title')}")
                        if 'summary' in item:
                            summary = item.get('summary', '')
                            print(f"   Summary: {summary[:100]}..." if len(summary) > 100 else f"   Summary: {summary}")
                        print()
            
            # Save the full response to a file
            with open("postman_exact_response.json", "w") as f:
                json.dump(data, f, indent=2)
                print("Full response saved to postman_exact_response.json")
        except json.JSONDecodeError:
            print(f"Error: Response is not valid JSON: {response.text[:200]}...")
    else:
        print(f"Error: {response.text}")
        
except Exception as e:
    print(f"Exception occurred: {str(e)}")
