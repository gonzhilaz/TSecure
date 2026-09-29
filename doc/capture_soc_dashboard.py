import os
import time
from playwright.sync_api import sync_playwright

def capture():
    out_dir = r"d:\DEVELOPMENT\Projects\Riski\TelkomSecure\doc\images"
    os.makedirs(out_dir, exist_ok=True)
    
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        page = browser.new_page(viewport={"width": 1440, "height": 960})
        page.goto("http://localhost:3005", wait_until="domcontentloaded")
        time.sleep(3)
        
        # 1. Default Tab (Helpdesk / Customer Care)
        p1 = os.path.join(out_dir, "soc_dashboard_helpdesk.png")
        page.screenshot(path=p1)
        print(f"Captured: {p1}")
        
        # 2. Click Overview tab (first tab)
        tabs = page.locator("div.grid button")
        if tabs.count() >= 4:
            tabs.nth(0).click()
            time.sleep(1.5)
            p2 = os.path.join(out_dir, "soc_dashboard_overview.png")
            page.screenshot(path=p2)
            print(f"Captured: {p2}")
            
            # 3. Click Device Integrity tab (third tab)
            tabs.nth(2).click()
            time.sleep(1.5)
            p3 = os.path.join(out_dir, "soc_dashboard_device_integrity.png")
            page.screenshot(path=p3)
            print(f"Captured: {p3}")
            
            # 4. Click NDP Simulator tab (fourth tab)
            tabs.nth(3).click()
            time.sleep(1.5)
            p4 = os.path.join(out_dir, "soc_dashboard_ndp_simulator.png")
            page.screenshot(path=p4)
            print(f"Captured: {p4}")
            
            # 5. Back to tab 1 (Helpdesk) and click first row to open detail modal
            tabs.nth(1).click()
            time.sleep(1)
            row = page.locator("tbody tr").first
            if row.is_visible():
                row.click()
                time.sleep(1.5)
                p_modal = os.path.join(out_dir, "soc_dashboard_customer_detail.png")
                page.screenshot(path=p_modal)
                print(f"Captured: {p_modal}")
        
        browser.close()
        print("All SOC Dashboard captures completed!")

if __name__ == "__main__":
    capture()
