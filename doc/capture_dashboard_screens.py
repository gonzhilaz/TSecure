import os
import time
from playwright.sync_api import sync_playwright

def capture():
    output_dir = r"d:\DEVELOPMENT\Projects\Riski\TelkomSecure\doc\images"
    os.makedirs(output_dir, exist_ok=True)
    
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        page = browser.new_page(viewport={"width": 1440, "height": 900})
        
        print("Navigating to http://localhost:3000...")
        page.goto("http://localhost:3000", wait_until="networkidle")
        time.sleep(2)
        
        # 1. Overview tab
        path1 = os.path.join(output_dir, "dashboard_tab_overview.png")
        page.screenshot(path=path1)
        print(f"Captured: {path1}")
        
        # 2. Click Customer Care & License Desk tab
        page.locator("button:has-text('Customer Care & License Desk')").click()
        time.sleep(1)
        path2 = os.path.join(output_dir, "dashboard_tab_helpdesk.png")
        page.screenshot(path=path2)
        print(f"Captured: {path2}")
        
        # Open customer detail modal from helpdesk tab
        first_row = page.locator("tbody tr").first
        if first_row.is_visible():
            first_row.click()
            time.sleep(1)
            path_modal = os.path.join(output_dir, "dashboard_customer_detail_modal.png")
            page.screenshot(path=path_modal)
            print(f"Captured: {path_modal}")
            
            # Close modal
            page.keyboard.press("Escape")
            time.sleep(0.5)
            
        # 3. Click Device Integrity & SIM Watch tab
        page.locator("button:has-text('Device Integrity & SIM Watch')").click()
        time.sleep(1)
        path3 = os.path.join(output_dir, "dashboard_tab_device_integrity.png")
        page.screenshot(path=path3)
        print(f"Captured: {path3}")
        
        # 4. Click NDP & Billing Simulator tab
        page.locator("button:has-text('NDP & Billing Simulator')").click()
        time.sleep(1)
        path4 = os.path.join(output_dir, "dashboard_tab_ndp_simulator.png")
        page.screenshot(path=path4)
        print(f"Captured: {path4}")
        
        browser.close()
        print("All dashboard screenshots captured successfully!")

if __name__ == "__main__":
    capture()
